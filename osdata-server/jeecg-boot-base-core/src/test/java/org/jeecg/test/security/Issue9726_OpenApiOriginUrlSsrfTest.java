package org.jeecg.test.security;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 【issues/9726】OpenAPI originUrl 存储型 SSRF 漏洞修复 (CWE-918) — 单元测试
 *
 * ── 漏洞是怎么引起的 ──────────────────────────────────────────────────────────
 * OpenApiController#call(/openapi/call/{path}) 是一个"服务端转发"接口：它读取数据库里
 * 配置的 originUrl，由【服务器】自己发起 restTemplate.exchange(...) 去请求该地址。
 * originUrl 由管理员通过 POST /openapi/add、PUT /openapi/edit 写入并持久化（存储型）。
 *
 * 自 issues/9590 起，originUrl 允许填写完整的 http(s)://host URL（用于微服务跨模块调用，
 * 如部署在 erp 7003 的接口）。修复前 validOriginUrl 的【完整URL分支】只校验了协议——
 * 禁止 file/ftp/gopher/jar/netdoc，却【完全不限制 host】。于是管理员可存入：
 *     originUrl = http://169.254.169.254/latest/meta-data/   (云元数据)
 *     originUrl = http://127.0.0.1:6379/                     (本机 redis 等内网服务)
 *     originUrl = http://attacker.com/                       (任意公网，把服务器当代理)
 * 再以合法 OpenAPI 凭证触发 /openapi/call/{path}，服务器即主动访问该地址 —— 存储型 SSRF。
 * 根因：完整URL分支"校验协议、放过主机"，被校验的内容覆盖不到真正危险的 host。
 *
 * ── 修复 ──────────────────────────────────────────────────────────────────────
 * 在 validOriginUrl 完整URL分支补两套互补的 host 校验（两者交集才放行）：
 *   1) SsrfFileTypeFilter.checkSsrfHttpUrl(url)：拦回环(127.x/::1)与链路本地
 *      (169.254.x，含云元数据 169.254.169.254)；放行 RFC1918 与公网。
 *   2) CommonUtils.checkInternalUrl(url)：拦公网地址；放行回环/局域网/链路本地。
 * 取交集后【仅 RFC1918 内网地址可通过】——既保留微服务跨模块内网调用，又同时堵死
 * 回环、链路本地/元数据、公网三类 SSRF 目标。单独任一方法都不足以覆盖（见各自反例）。
 *
 * ── 测试策略 ──────────────────────────────────────────────────────────────────
 * 校验逻辑都在 public static 方法里，纯单测、无需 Spring / 无需联网（全部用字面量 IP，
 * 不触发 DNS）。下面用 validateFullOriginUrl 复刻 controller 完整URL分支的两步校验，
 * 对四类目标分别断言放行/拦截。
 *
 * @author liusq
 * @date 2026-06-29
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9726_OpenApiOriginUrlSsrfTest {

    /**
     * 复刻 OpenApiController#validOriginUrl 完整URL分支在修复后执行的两步 host 校验。
     * 任一步抛 JeecgBootException 即视为该 URL 被拦截。
     */
    private void validateFullOriginUrl(String originUrl) {
        SsrfFileTypeFilter.checkSsrfHttpUrl(originUrl); // 拦回环 + 链路本地(含元数据)
        CommonUtils.checkInternalUrl(originUrl);        // 拦公网
    }

    // ==================================================================
    // 一、漏洞复现：四类危险目标在修复后都被拦截
    // ==================================================================

    @Nested
    @DisplayName("漏洞复现 —— 修复后存入完整URL的各类 SSRF 目标均被拦死")
    class MaliciousTargetsBlocked {

        @Test
        @DisplayName("【元数据】云元数据端点 169.254.169.254 被拦截（checkSsrfHttpUrl 兜底）")
        void cloudMetadataBlocked() {
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> validateFullOriginUrl("http://169.254.169.254/latest/meta-data/"));
            System.out.println("[cloudMetadataBlocked] 169.254.169.254 -> " + ex.getMessage());
            assertTrue(ex.getMessage().contains("链路本地") || ex.getMessage().contains("本机"),
                    "云元数据属链路本地地址，应被 checkSsrfHttpUrl 拦截");
        }

        @Test
        @DisplayName("【回环】本机服务 127.0.0.1:6379 被拦截（checkSsrfHttpUrl 兜底）")
        void loopbackBlocked() {
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> validateFullOriginUrl("http://127.0.0.1:6379/"));
            System.out.println("[loopbackBlocked] 127.0.0.1:6379 -> " + ex.getMessage());
            assertTrue(ex.getMessage().contains("本机") || ex.getMessage().contains("链路本地"),
                    "回环地址应被 checkSsrfHttpUrl 拦截");
        }

        @Test
        @DisplayName("【公网】任意公网地址 8.8.8.8 被拦截（checkInternalUrl 兜底）")
        void publicAddressBlocked() {
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> validateFullOriginUrl("http://8.8.8.8/x"));
            System.out.println("[publicAddressBlocked] 8.8.8.8 -> " + ex.getMessage());
            assertTrue(ex.getMessage().contains("内网"),
                    "公网地址应被 checkInternalUrl 拦截，仅允许内网");
        }
    }

    // ==================================================================
    // 二、合法功能不被误伤：微服务跨模块内网(RFC1918)调用放行
    // ==================================================================

    @Nested
    @DisplayName("合法功能 —— 微服务跨模块内网(RFC1918)地址正常放行")
    class LegitInternalTargetsPass {

        @Test
        @DisplayName("RFC1918 内网地址（10/172.16/192.168）放行，不抛异常")
        void rfc1918AddressesPass() {
            assertDoesNotThrow(() -> validateFullOriginUrl("http://10.0.0.8:7003/erp/order/list"));
            assertDoesNotThrow(() -> validateFullOriginUrl("http://172.16.5.20:8080/api"));
            assertDoesNotThrow(() -> validateFullOriginUrl("http://192.168.1.30:9999/house/list"));
            System.out.println("[rfc1918AddressesPass] RFC1918 内网地址全部放行，微服务跨模块调用不受影响");
        }
    }

    // ==================================================================
    // 三、为什么必须两套校验【组合】—— 单独任一方法都有漏网之鱼
    // ==================================================================

    @Nested
    @DisplayName("互补性证明 —— 单独任一校验都不足以覆盖全部 SSRF 目标")
    class WhyBothChecksNeeded {

        @Test
        @DisplayName("仅 checkInternalUrl 不够：它把链路本地视为内网，会放过云元数据 169.254.169.254")
        void internalCheckAloneLeaksMetadata() {
            // checkInternalUrl 单独使用时，链路本地地址被当作"内网"放行 —— 这正是必须叠加
            // checkSsrfHttpUrl 的原因
            assertDoesNotThrow(() -> CommonUtils.checkInternalUrl("http://169.254.169.254/latest/meta-data/"));
            System.out.println("[internalCheckAloneLeaksMetadata] 仅 checkInternalUrl 会放过云元数据 → 需 checkSsrfHttpUrl 兜底");
        }

        @Test
        @DisplayName("仅 checkSsrfHttpUrl 不够：它放行公网，会把服务器当作访问任意公网的代理")
        void ssrfCheckAloneLeaksPublic() {
            // checkSsrfHttpUrl 单独使用时，公网地址被放行 —— 这正是必须叠加 checkInternalUrl 的原因
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl("http://8.8.8.8/x"));
            System.out.println("[ssrfCheckAloneLeaksPublic] 仅 checkSsrfHttpUrl 会放过公网地址 → 需 checkInternalUrl 兜底");
        }
    }
}
