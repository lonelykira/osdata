package org.jeecg.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 【QQYUN-15536】修复上传SVG文件通过静态资源路径触发存储型XSS
 * 对 .svg 后缀的响应自动追加以下安全响应头，阻止浏览器在顶层文档渲染 SVG 时执行内嵌脚本
 * Content-Security-Policy: sandbox —— 沙箱化页面，禁止脚本、表单提交、同源操作等
 * X-Content-Type-Options: nosniff —— 禁止 MIME 类型嗅探，防止绕过 Content-Type 限制
 * 对通过 {@code <img src="xxx.svg">} 方式内嵌的 SVG 无任何影响（子资源加载不受此类头部限制）
 * @author liusq
 * @date 2026/05/25
 * @see <a href="https://github.com/jeecgboot/JeecgBoot/issues/9646">issues/9646</a>
 */
@Slf4j
public class SvgSecurityFilter extends OncePerRequestFilter {

    private static final String SVG_SUFFIX = ".svg";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //update-begin---author:liusq ---date:2026-05-26  for：【QQYUN-15536】补充URL编码绕过防御：getServletPath()由Servlet容器解码，可防止%2E等编码绕过-----------
        // 使用 getServletPath() 而非 getRequestURI()：前者由 Servlet 容器自动 URL 解码，
        // 后者返回原始未解码路径，攻击者可用 %2e%73%76%67 绕过 endsWith(".svg") 检测
        String uri = request.getServletPath();
        //update-end---author:liusq ---date:2026-05-26  for：【QQYUN-15536】补充URL编码绕过防御：getServletPath()由Servlet容器解码，可防止%2E等编码绕过-----------
        if (uri != null && uri.toLowerCase().endsWith(SVG_SUFFIX)) {
            // 沙箱化限制：即使浏览器以顶层文档方式访问 SVG，内嵌脚本也无法执行
            response.setHeader("Content-Security-Policy", "sandbox");
            // 禁止 MIME 类型嗅探，强制浏览器遵守声明的 Content-Type，防止绕过
            response.setHeader("X-Content-Type-Options", "nosniff");
        }
        filterChain.doFilter(request, response);
    }
}
