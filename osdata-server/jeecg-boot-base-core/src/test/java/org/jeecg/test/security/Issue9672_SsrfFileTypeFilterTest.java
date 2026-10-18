package org.jeecg.test.security;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SsrfFileTypeFilter.checkSsrfHttpUrl SSRF 防护单元测试
 *
 * 覆盖：
 *  - 【issues/9672】RFC1918 私网地址（10.x / 172.16-31.x / 192.168.x）应放行，兼容企业内网 MinIO/OSS
 *  - 回归：loopback / link-local 仍拦截
 *  - 回归：公网 URL 正常放行
 *  - 协议校验、空值校验
 *
 * @author wangshuai
 * @date 2026-06-16
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9672_SsrfFileTypeFilterTest {

    @Nested
    @DisplayName("【issues/9672】RFC1918 私网地址应放行（兼容内网 MinIO/OSS）")
    class Issue9672_PrivateNetwork {

        @Test
        @DisplayName("10.0.0.0/8 段应放行（内网 MinIO 等服务）")
        void shouldAllow_10Network() {
            String url = "http://10.0.0.1/secret.txt";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF校验] " + url + "  ->  放行(RFC1918 私网)");
        }

        @Test
        @DisplayName("172.16.0.0/12 段应放行（内网 MinIO 等服务）")
        void shouldAllow_172_16Network() {
            String url = "http://172.16.0.1:9000/bucket/file.pdf";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF校验] " + url + "  ->  放行(RFC1918 私网)");
        }

        @Test
        @DisplayName("192.168.0.0/16 段应放行（内网 MinIO 等服务）")
        void shouldAllow_192_168Network() {
            String url = "http://192.168.1.100/internal/data.txt";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF校验] " + url + "  ->  放行(RFC1918 私网)");
        }
    }

    @Nested
    @DisplayName("回归：loopback / link-local 仍拦截")
    class Regression_LoopbackLinkLocal {

        @Test
        @DisplayName("127.0.0.1 应被拦截")
        void shouldBlock_loopback_ipv4() {
            String url = "http://127.0.0.1/etc/passwd";
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            assertTrue(ex.getMessage().contains("本机或链路本地地址"));
            System.out.println("    [SSRF校验] " + url + "  ->  拦截: " + ex.getMessage());
        }

        @Test
        @DisplayName("localhost 应被拦截")
        void shouldBlock_localhost() {
            String url = "http://localhost:8080/admin";
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF校验] " + url + "  ->  拦截: " + ex.getMessage());
        }

        @Test
        @DisplayName("169.254.169.254 云元数据应被拦截")
        void shouldBlock_cloudMetadata() {
            String url = "http://169.254.169.254/latest/meta-data/";
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF校验] " + url + "  ->  拦截(云元数据): " + ex.getMessage());
        }
    }

    @Nested
    @DisplayName("公网 URL 正常放行")
    class PublicUrl_AllowThrough {

        @Test
        @DisplayName("HTTPS 公网 URL 应放行")
        void shouldAllow_publicHttps() {
            String url = "https://www.baidu.com/index.html";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF校验] " + url + "  ->  放行(公网)");
        }

        @Test
        @DisplayName("HTTP 公网 URL 应放行")
        void shouldAllow_publicHttp() {
            String url = "http://cdn.example.com/file.pdf";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF校验] " + url + "  ->  放行(公网)");
        }
    }

    @Nested
    @DisplayName("协议与格式校验")
    class ProtocolAndFormat {

        @Test
        @DisplayName("空 URL 应拦截")
        void shouldBlock_emptyUrl() {
            assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(""));
            assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(null));
            System.out.println("    [SSRF校验] 空值 \"\" / null  ->  拦截");
        }

        @Test
        @DisplayName("非 http/https 协议应拦截")
        void shouldBlock_nonHttpProtocol() {
            for (String url : new String[]{"file:///etc/passwd", "ftp://192.168.1.1/data", "gopher://127.0.0.1:6379/_INFO"}) {
                JeecgBootException ex = assertThrows(JeecgBootException.class,
                        () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
                System.out.println("    [SSRF校验] " + url + "  ->  拦截(非 http/https): " + ex.getMessage());
            }
        }

        @Test
        @DisplayName("格式错误的 URL 应拦截")
        void shouldBlock_malformedUrl() {
            String url = "http://";
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF校验] " + url + "  ->  拦截(格式错误): " + ex.getMessage());
        }

        @Test
        @DisplayName("无法解析的主机名应拦截")
        void shouldBlock_unresolvableHost() {
            String host = "this-host-does-not-exist-12345.invalid";
            String url = "http://" + host + "/test";
            // 某些网络环境存在通配 DNS（把任意不存在域名解析到一个真实公网地址），
            // 此时该 host 并非"无法解析"，本用例前提不成立，跳过以免环境误报。
            boolean resolvable;
            try {
                java.net.InetAddress.getByName(host);
                resolvable = true;
            } catch (java.net.UnknownHostException e) {
                resolvable = false;
            }
            org.junit.jupiter.api.Assumptions.assumeFalse(resolvable,
                    "当前网络存在通配 DNS，" + host + " 被解析为真实地址，跳过该用例");

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF校验] " + url + "  ->  拦截(主机无法解析): " + ex.getMessage());
        }
    }
}
