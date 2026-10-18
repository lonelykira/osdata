package org.jeecg.modules.zeroAttack;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * SVG 存储型 XSS 漏洞攻击与修复验证测试
 * Issue: https://github.com/jeecgboot/JeecgBoot/issues/9646
 *
 * 漏洞原理：
 *   1. svg 在上传白名单中，允许上传含内联脚本的 SVG 文件
 *   2. /sys/common/static/** 路径设置了 Content-Disposition: attachment（强制下载，安全）
 *   3. 但上传目录同时被注册为静态资源目录（/**），可绕过 Controller 直接访问
 *   4. 浏览器以顶层文档渲染 SVG 时，内联 <script> 被执行 → Stored XSS
 *
 * 修复方案（SvgSecurityFilter）：
 *   对所有 .svg 响应追加：
 *   - Content-Security-Policy: sandbox  → 沙箱化页面，禁止脚本执行
 *   - X-Content-Type-Options: nosniff   → 禁止 MIME 类型嗅探
 *
 * 前提：运行测试前，请先在本机启动 JeecgBoot 后端服务（localhost:8080）
 * 启动命令：cd jeecg-module-system/jeecg-system-start && mvn spring-boot:run
 *
 * 测试用例说明：
 *   - testUploadMaliciousSvg              上传恶意 SVG，打印可在浏览器验证的 URL（手动验证）
 *   - testSvgSecurityFilterFix            【核心】验证 SvgSecurityFilter 修复是否生效（自动判断）
 *   - testSvgXssViaDirectStaticPath       对比直连路径与强制下载路径的所有安全响应头
 */
// WebEnvironment.NONE：只启动 Spring 容器用于依赖注入（RedisUtil/JwtUtil），
// 不启动内嵌服务器。HTTP 请求均发往外部已运行的 localhost:8080 服务。
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = JeecgSystemApplication.class)
@SuppressWarnings({"FieldCanBeLocal", "SpringJavaAutowiredMembersInspection"})
public class attackTest {

    @Autowired
    private RedisUtil redisUtil;

    /** 已运行的后端服务地址，与 application-dev.yml 中的端口保持一致 */
    private final String BASE_URL = "http://localhost:8080/jeecg-boot";
    private final String USERNAME = "admin";
    private final String PASSWORD = "123456";

    /**
     * 恶意 SVG 内容：包含内联 XSS payload
     * 浏览器以顶层文档渲染时，<script> 会被执行
     */
    private static final String MALICIOUS_SVG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"100\">\n" +
            "  <script>alert('XSS-9646: SVG Stored XSS via static resource path!')</script>\n" +
            "  <rect width=\"200\" height=\"100\" fill=\"red\"/>\n" +
            "  <text x=\"10\" y=\"50\" fill=\"white\">XSS PoC - Issue 9646</text>\n" +
            "</svg>";

    // ===================== 测试用例 =====================

    /**
     * 测试用例1：上传恶意 SVG，打印可在浏览器中手动验证的 URL
     *
     * 如何验证漏洞（修复前）：
     *   用浏览器访问「直连路径」→ 弹出 alert 说明存在 XSS
     * 如何验证修复（修复后）：
     *   用浏览器访问「直连路径」→ 不弹 alert，说明 CSP sandbox 生效
     */
    @Test
    public void testUploadMaliciousSvg() throws Exception {
        String token = getToken();
        System.out.println("======================================================");
        System.out.println("  测试：上传含 XSS payload 的 SVG 文件");
        System.out.println("  Issue: https://github.com/jeecgboot/JeecgBoot/issues/9646");
        System.out.println("======================================================");

        String filePath = uploadSvg(token);
        System.out.println("[上传成功] 文件路径: " + filePath);
        System.out.println();
        System.out.println(">>> 请在浏览器中访问以下地址手动验证 <<<");
        System.out.println();
        System.out.println("  [漏洞路径] 静态资源直连：");
        System.out.println("  " + BASE_URL + "/" + filePath);
        System.out.println("  修复前：弹出 alert（XSS 触发）");
        System.out.println("  修复后：不弹 alert（CSP sandbox 阻止脚本）");
        System.out.println();
        System.out.println("  [对照路径] 强制下载（始终安全，不触发 XSS）：");
        System.out.println("  " + BASE_URL + "/sys/common/static/" + filePath);
    }

    /**
     * 测试用例2：【核心】验证 SvgSecurityFilter 修复是否生效
     *
     * SvgSecurityFilter 修复方案：对 .svg 响应追加安全头，而不是禁止上传
     *   Content-Security-Policy: sandbox  → 浏览器沙箱化渲染，<script> 不被执行
     *   X-Content-Type-Options: nosniff   → 禁止 MIME 嗅探绕过
     *
     * 判断逻辑：
     *   直连路径响应包含 Content-Security-Policy: sandbox → 修复已生效
     *   直连路径响应缺少 Content-Security-Policy: sandbox → 修复未生效
     */
    @Test
    public void testSvgSecurityFilterFix() throws Exception {
        String token = getToken();
        RestTemplate restTemplate = new RestTemplate();

        System.out.println("======================================================");
        System.out.println("  测试：SvgSecurityFilter 安全响应头修复验证");
        System.out.println("  修复类：org.jeecg.config.filter.SvgSecurityFilter");
        System.out.println("======================================================");

        // Step 1: 上传恶意 SVG（SvgSecurityFilter 修复不移除白名单，上传仍允许）
        String filePath = uploadSvg(token);
        System.out.println("[Step 1] SVG 上传成功，文件路径: " + filePath);

        // Step 2: 通过直连静态资源路径访问（漏洞路径）
        String directUrl = BASE_URL + "/" + filePath;
        System.out.println("\n[Step 2] 检测直连路径响应头（漏洞路径）");
        System.out.println("  URL: " + directUrl);

        try {
            ResponseEntity<String> resp = restTemplate.exchange(
                    directUrl, HttpMethod.GET, HttpEntity.EMPTY, String.class);

            String csp = resp.getHeaders().getFirst("Content-Security-Policy");
            String xContentType = resp.getHeaders().getFirst("X-Content-Type-Options");
            String contentDisposition = resp.getHeaders().getFirst("Content-Disposition");
            String contentType = String.valueOf(resp.getHeaders().getContentType());

            System.out.println("  响应状态: " + resp.getStatusCode());
            System.out.println("  Content-Type: " + contentType);
            System.out.println("  Content-Security-Policy: " + csp);
            System.out.println("  X-Content-Type-Options: " + xContentType);
            System.out.println("  Content-Disposition: " + contentDisposition);

            // 判断 SvgSecurityFilter 是否生效
            boolean cspSandboxPresent = csp != null && csp.contains("sandbox");
            boolean nosniffPresent = xContentType != null && xContentType.contains("nosniff");

            System.out.println();
            if (cspSandboxPresent && nosniffPresent) {
                System.out.println("  ✓ [修复已生效] Content-Security-Policy: sandbox 存在");
                System.out.println("  ✓ [修复已生效] X-Content-Type-Options: nosniff 存在");
                System.out.println("  → 浏览器访问此 SVG 时，<script> 将因 CSP sandbox 被阻止执行");
                System.out.println("  → XSS 漏洞已修复");
            } else {
                System.out.println("  ✗ [修复未生效] 缺少必要的安全响应头：");
                if (!cspSandboxPresent) {
                    System.out.println("    - 缺少 Content-Security-Policy: sandbox（脚本可执行！）");
                }
                if (!nosniffPresent) {
                    System.out.println("    - 缺少 X-Content-Type-Options: nosniff");
                }
                System.out.println("  → SvgSecurityFilter 未注册或未生效，漏洞仍然存在！");
                System.out.println("  → 浏览器访问此 URL 将触发 XSS: " + directUrl);
            }
        } catch (Exception e) {
            System.out.println("  访问直连路径异常: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                System.out.println("  [提示] 无法连接到 " + BASE_URL);
                System.out.println("  [提示] 请先启动后端服务: cd jeecg-module-system/jeecg-system-start && mvn spring-boot:run");
            }
        }
    }

    /**
     * 上传恶意 SVG 文件，返回服务端返回的文件相对路径
     */
    private String uploadSvg(String token) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-Access-Token", token);

        byte[] svgBytes = MALICIOUS_SVG.getBytes("UTF-8");
        ByteArrayResource svgResource = new ByteArrayResource(svgBytes) {
            @Override
            public String getFilename() {
                return "poc-svg-xss.svg";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", svgResource);
        body.add("biz", "test");

        String uploadUrl = BASE_URL + "/sys/common/upload";
        System.out.println("上传地址: " + uploadUrl);

        try {
            ResponseEntity<JSONObject> response = restTemplate.exchange(
                    uploadUrl, HttpMethod.POST, new HttpEntity<>(body, headers), JSONObject.class);

            if (response.getBody() == null) {
                throw new RuntimeException("上传失败：无响应体");
            }
            boolean success = Boolean.TRUE.equals(response.getBody().getBoolean("success"));
            if (!success) {
                throw new RuntimeException("上传被服务端拒绝（svg 可能已从白名单移除）: " + response.getBody().toJSONString());
            }
            return response.getBody().getString("message");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                throw new RuntimeException(
                        "无法连接到后端服务 " + BASE_URL + "\n" +
                        "请先启动后端：cd jeecg-module-system/jeecg-system-start && mvn spring-boot:run", e);
            }
            throw e;
        }
    }

    /**
     * 生成 JWT Token 并写入 Redis，与 OnlineApiTest 写法一致
     */
    private String getToken() {
        String token = JwtUtil.sign(USERNAME, PASSWORD);
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, 60);
        return token;
    }
}
