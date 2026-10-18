package org.jeecg.modules.airag.llm.document;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

/**
 * 网页解析器，使用Jsoup爬取网页并转换为Markdown格式
 *
 * @author sjlei
 * @date 2026/3/19
 */
@Slf4j
public class WebPageParser {

    /**
     * 请求超时时间(毫秒)
     */
    private static final int TIMEOUT_MS = 15000;

    /**
     * 最大body大小(5MB)
     */
    private static final int MAX_BODY_SIZE = 5 * 1024 * 1024;

    /**
     * User-Agent
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * 最大重定向次数
     */
    private static final int MAX_REDIRECTS = 5;

    /**
     * 爬取网页并转换为Markdown
     *
     * @param url 网页URL
     * @return Markdown格式的文本内容
     * @throws IOException 网络请求失败时抛出
     */
    public String parseToMarkdown(String url) throws IOException {
        //update-begin---author:zhangdaihao ---date:2026-08-06  for：【issues/9808】AI知识库Web文档抓取SSRF漏洞修复，禁止自动跳转并逐跳校验-----------
        String currentUrl = url;
        Connection.Response response = null;
        for (int i = 0; i <= MAX_REDIRECTS; i++) {
            // 每一跳（含初始 URL）都重新做 SSRF 校验，拦截 loopback / link-local / 云元数据地址
            SsrfFileTypeFilter.checkSsrfHttpUrl(currentUrl);

            response = Jsoup.connect(currentUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .maxBodySize(MAX_BODY_SIZE)
                    // 允许任意 content-type（如 302 无 Content-Type），避免校验重定向前被 Jsoup 拦截
                    .ignoreContentType(true)
                    // 关键：禁止 Jsoup 自动跟随重定向，否则 SSRF 校验会被绕过
                    .followRedirects(false)
                    .execute();

            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                String location = response.header("Location");
                if (oConvertUtils.isEmpty(location)) {
                    throw new JeecgBootException("非法重定向：Location 为空");
                }
                // 相对 Location 解析为绝对地址后回到循环顶部再次校验
                currentUrl = new URL(response.url(), location).toString();
                continue;
            }
            break;
        }

        if (response == null) {
            throw new JeecgBootException("非法URL：请求失败");
        }
        int finalStatus = response.statusCode();
        if (finalStatus >= 300 && finalStatus < 400) {
            throw new JeecgBootException("非法URL：重定向次数过多");
        }

        Document doc = response.parse();
        //update-end---author:zhangdaihao ---date:2026-08-06  for：【issues/9808】AI知识库Web文档抓取SSRF漏洞修复，禁止自动跳转并逐跳校验-----------

        // 移除脚本、样式、导航、页脚等无关元素
        doc.select("script, style, nav, footer, header, iframe, noscript, svg, form, button, input, select, textarea, .sidebar, .nav, .menu, .footer, .header, .ad, .advertisement, .comment, .comments").remove();

        // 优先提取正文区域
        Element body = extractMainContent(doc);

        StringBuilder markdown = new StringBuilder();

        // 提取页面标题
        String title = doc.title();
        if (title != null && !title.trim().isEmpty()) {
            markdown.append("# ").append(title.trim()).append("\n\n");
        }

        // 将HTML转为Markdown
        convertToMarkdown(body, markdown);

        return cleanMarkdown(markdown.toString());
    }

    /**
     * 提取正文区域，优先使用article/main标签，否则使用body
     */
    private Element extractMainContent(Document doc) {
        // 按优先级尝试获取正文容器
        String[] selectors = {"article", "main", "[role=main]", ".content", ".post-content", ".article-content", ".entry-content", "#content"};
        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty() && elements.first().text().length() > 100) {
                return elements.first();
            }
        }
        return doc.body() != null ? doc.body() : doc;
    }

    /**
     * 递归将HTML元素转换为Markdown
     */
    private void convertToMarkdown(Element element, StringBuilder sb) {
        for (Node child : element.childNodes()) {
            if (child instanceof TextNode) {
                String text = ((TextNode) child).text().trim();
                if (!text.isEmpty()) {
                    sb.append(text);
                }
            } else if (child instanceof Element) {
                Element el = (Element) child;
                String tagName = el.tagName().toLowerCase();

                switch (tagName) {
                    case "h1":
                        sb.append("\n\n# ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h2":
                        sb.append("\n\n## ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h3":
                        sb.append("\n\n### ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h4":
                        sb.append("\n\n#### ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h5":
                        sb.append("\n\n##### ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h6":
                        sb.append("\n\n###### ").append(el.text().trim()).append("\n\n");
                        break;
                    case "p":
                        sb.append("\n\n");
                        convertToMarkdown(el, sb);
                        sb.append("\n\n");
                        break;
                    case "br":
                        sb.append("\n");
                        break;
                    case "strong":
                    case "b":
                        sb.append("**").append(el.text().trim()).append("**");
                        break;
                    case "em":
                    case "i":
                        sb.append("*").append(el.text().trim()).append("*");
                        break;
                    case "code":
                        sb.append("`").append(el.text()).append("`");
                        break;
                    case "pre":
                        sb.append("\n\n```\n").append(el.text()).append("\n```\n\n");
                        break;
                    case "a":
                        String href = el.attr("abs:href");
                        String linkText = el.text().trim();
                        if (!linkText.isEmpty() && !href.isEmpty()) {
                            sb.append("[").append(linkText).append("](").append(href).append(")");
                        } else if (!linkText.isEmpty()) {
                            sb.append(linkText);
                        }
                        break;
                    case "img":
                        String src = el.attr("abs:src");
                        String alt = el.attr("alt");
                        // 只保留http(s)开头的真实图片URL，过滤掉base64内联图片
                        if (!src.isEmpty() && src.startsWith("http")) {
                            sb.append("![").append(alt != null ? alt : "").append("](").append(src).append(")");
                        }
                        break;
                    case "ul":
                        sb.append("\n");
                        for (Element li : el.children()) {
                            if ("li".equals(li.tagName())) {
                                sb.append("- ").append(li.text().trim()).append("\n");
                            }
                        }
                        sb.append("\n");
                        break;
                    case "ol":
                        sb.append("\n");
                        int idx = 1;
                        for (Element li : el.children()) {
                            if ("li".equals(li.tagName())) {
                                sb.append(idx++).append(". ").append(li.text().trim()).append("\n");
                            }
                        }
                        sb.append("\n");
                        break;
                    case "blockquote":
                        String[] lines = el.text().trim().split("\n");
                        sb.append("\n");
                        for (String line : lines) {
                            sb.append("> ").append(line).append("\n");
                        }
                        sb.append("\n");
                        break;
                    case "table":
                        convertTableToMarkdown(el, sb);
                        break;
                    case "hr":
                        sb.append("\n\n---\n\n");
                        break;
                    case "div":
                    case "section":
                    case "span":
                    case "figure":
                    case "figcaption":
                        convertToMarkdown(el, sb);
                        break;
                    default:
                        convertToMarkdown(el, sb);
                        break;
                }
            }
        }
    }

    /**
     * 将HTML表格转为Markdown表格
     */
    private void convertTableToMarkdown(Element table, StringBuilder sb) {
        Elements rows = table.select("tr");
        if (rows.isEmpty()) {
            return;
        }

        sb.append("\n\n");
        boolean headerDone = false;

        for (Element row : rows) {
            Elements cells = row.select("th, td");
            if (cells.isEmpty()) {
                continue;
            }

            sb.append("|");
            for (Element cell : cells) {
                sb.append(" ").append(cell.text().trim()).append(" |");
            }
            sb.append("\n");

            // 在第一行后添加分隔线
            if (!headerDone) {
                sb.append("|");
                for (int i = 0; i < cells.size(); i++) {
                    sb.append(" --- |");
                }
                sb.append("\n");
                headerDone = true;
            }
        }
        sb.append("\n");
    }

    /**
     * 清理Markdown文本：去除多余空行、首尾空白
     */
    private String cleanMarkdown(String markdown) {
        // 去除连续3个以上换行为2个换行
        markdown = markdown.replaceAll("\n{3,}", "\n\n");
        // 去除行首尾空白(保留换行)
        markdown = markdown.replaceAll("(?m)^[ \t]+|[ \t]+$", "");
        return markdown.trim();
    }
}
