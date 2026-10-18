package org.jeecg.test.security;

import com.sun.net.httpserver.HttpServer;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.FileDownloadUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

/**
 * 【issues/9681】SSRF 重定向绕过漏洞修复 (CWE-918) — 单元测试
 *
 * ── 漏洞 ──────────────────────────────────────────────────────────────────────
 * 修复前：download2DiskFromNet / getDownInputStream 仅在打开连接前对 fileUrl 做了
 * 一次 SsrfFileTypeFilter.checkSsrfHttpUrl(...)，随后用 HttpURLConnection 打开连接，
 * 而 JDK 默认 followRedirects=true 会【自动跟随 3xx 重定向】。
 *
 * 攻击者：传入公网地址 http://attacker.com/x（首检通过），其服务器返回
 *   302 Location: http://127.0.0.1:xxx/... 或 http://169.254.169.254/latest/meta-data/。
 * JDK 自动跟随该跳且【不再复检】，于是访问到内网/云元数据 —— SSRF 成立。
 * 根因：被校验的 URL 与最终被访问的 URL 不一致（重定向逃逸）。
 *
 * ── 修复 ──────────────────────────────────────────────────────────────────────
 * openSafeConnection(...)：setInstanceFollowRedirects(false) 关闭自动跳转，改为手动
 * 循环（最多 5 跳），对【初始 URL 及每一跳重定向目标】都重新 checkSsrfHttpUrl，
 * 相对 Location 解析为绝对地址后再校验，超过次数抛异常。
 *
 * ── 测试策略 ──────────────────────────────────────────────────────────────────
 * 1. 漏洞根因（用真实过滤器，无网络）：证明"公网首检通过、内网目标会被拦"，
 *    从而说明单次前置校验对重定向无效，必须逐跳复检。
 * 2. 修复行为（mockStatic + 本地 HttpServer）：本地测试服在 127.0.0.1，会被真实
 *    SSRF 过滤器拦截，故用 mockStatic 放行测试服，专注断言"每一跳都调用了
 *    checkSsrfHttpUrl"以及"某一跳校验失败会整体抛异常 / 不会自动跟随"。
 *
 * @author wangshuai
 * @date 2026-06-17
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9681_SsrfRedirectBypassTest {

    private static HttpServer server;
    private static String base;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

        // 302 相对跳转 /redirect -> /target，验证"相对 Location 解析 + 逐跳复检"
        server.createContext("/redirect", ex -> {
            ex.getResponseHeaders().add("Location", "/target");
            ex.sendResponseHeaders(302, -1);
            ex.close();
        });
        // 最终 200 资源
        server.createContext("/target", ex -> {
            byte[] body = "OK-BODY".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        // 直接 200，无重定向
        server.createContext("/direct", ex -> {
            byte[] body = "DIRECT".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        // 无限自跳，验证"最大重定向次数"保护
        server.createContext("/loop", ex -> {
            ex.getResponseHeaders().add("Location", "/loop");
            ex.sendResponseHeaders(302, -1);
            ex.close();
        });
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    /** 反射调用 private static FileDownloadUtils.openSafeConnection，并解包反射异常。 */
    private HttpURLConnection openSafeConnection(String url) throws Throwable {
        Method m = FileDownloadUtils.class.getDeclaredMethod("openSafeConnection", String.class);
        m.setAccessible(true);
        try {
            return (HttpURLConnection) m.invoke(null, url);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    // ==================================================================
    // 一、漏洞根因：单次前置校验无法防住重定向（真实过滤器，无网络）
    // ==================================================================

    @Nested
    @DisplayName("漏洞根因 —— 为什么单次前置校验会被重定向绕过")
    class RootCause {

        @Test
        @DisplayName("攻击者提供的公网 URL 首检通过（旧逻辑只校验这一次）")
        void publicUrlPassesFirstCheck() {
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl("http://cdn.example.com/x"));
            System.out.println("[publicUrlPassesFirstCheck] 公网 http://cdn.example.com/x 首检通过（攻击者入口）");
        }

        @Test
        @DisplayName("重定向目标(内网/元数据)若被复检则会拦截 —— 旧逻辑因自动跟随而漏检")
        void redirectTargetWouldBeBlockedIfRechecked() {
            // 这两个地址正是攻击者用 302 跳过去的目标；旧逻辑 JDK 自动跟随、不复检 → 绕过
            JeecgBootException e1 = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl("http://127.0.0.1:6379/"));
            JeecgBootException e2 = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl("http://169.254.169.254/latest/meta-data/"));
            System.out.println("[redirectTargetWouldBeBlockedIfRechecked] 127.0.0.1:6379 复检 -> " + e1.getMessage());
            System.out.println("[redirectTargetWouldBeBlockedIfRechecked] 169.254.169.254 复检 -> " + e2.getMessage());
            System.out.println("[redirectTargetWouldBeBlockedIfRechecked] 结论：校验的URL≠实际访问的URL，必须逐跳复检");
        }
    }

    // ==================================================================
    // 二、修复行为：openSafeConnection 逐跳复检 + 关闭自动跳转
    // ==================================================================

    @Nested
    @DisplayName("修复行为 —— openSafeConnection 对每一跳重定向都复检")
    class FixBehavior {

        @Test
        @DisplayName("发生重定向时，初始 URL 与重定向目标都各被 checkSsrfHttpUrl 校验一次")
        void everyHopIsValidated() throws Throwable {
            java.util.List<String> checked = new java.util.ArrayList<>();
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                // 默认放行（void 方法 mock 后即 no-op），让本地测试服可达；同时记录每一跳被校验的 URL
                mocked.when(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(anyString()))
                        .thenAnswer(inv -> { checked.add(inv.getArgument(0)); return null; });

                HttpURLConnection conn = openSafeConnection(base + "/redirect");

                System.out.println("[everyHopIsValidated] 被 SSRF 复检的每一跳 = " + checked);
                System.out.println("[everyHopIsValidated] 最终响应码 = " + conn.getResponseCode());

                assertEquals(200, conn.getResponseCode(), "应手动跟随到最终 200 资源");

                // 关键断言：初始跳与重定向目标都被复检 —— 证明没有走 JDK 自动跟随
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(base + "/redirect"), times(1));
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(base + "/target"), times(1));
                conn.disconnect();
            }
        }

        @Test
        @DisplayName("重定向目标被判定为危险地址时，整体抛 JeecgBootException（绕过被堵死）")
        void blockedRedirectTargetThrows() {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                // 模拟：初始公网放行，但重定向目标(/target，代表内网)校验失败
                mocked.when(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(anyString())).thenAnswer(inv -> {
                    String u = inv.getArgument(0);
                    if (u.contains("/target")) {
                        throw new JeecgBootException("非法URL：禁止访问本机或链路本地地址");
                    }
                    return null;
                });

                JeecgBootException ex = assertThrows(JeecgBootException.class,
                        () -> openSafeConnection(base + "/redirect"));
                System.out.println("[blockedRedirectTargetThrows] 重定向到内网被拦截 -> " + ex.getMessage());
                assertTrue(ex.getMessage().contains("禁止访问"),
                        "应在跟随重定向前就因目标地址校验失败而中断");
            }
        }

        @Test
        @DisplayName("无重定向的直连资源：仅校验一次并正常返回 200")
        void directResourceValidatedOnce() throws Throwable {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                HttpURLConnection conn = openSafeConnection(base + "/direct");
                System.out.println("[directResourceValidatedOnce] 直连无重定向，响应码 = " + conn.getResponseCode());
                assertEquals(200, conn.getResponseCode());
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(base + "/direct"), times(1));
                conn.disconnect();
            }
        }

        @Test
        @DisplayName("重定向次数超过上限(5)时抛异常，防止恶意死循环跳转")
        void tooManyRedirectsThrows() {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                JeecgBootException ex = assertThrows(JeecgBootException.class,
                        () -> openSafeConnection(base + "/loop"));
                System.out.println("[tooManyRedirectsThrows] 死循环跳转被阻断 -> " + ex.getMessage());
                assertTrue(ex.getMessage().contains("重定向次数过多"));
                // 初始 + 5 次重定向 = 共 6 次校验（i 从 0 到 5）
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(anyString()), times(6));
            }
        }
    }
}
