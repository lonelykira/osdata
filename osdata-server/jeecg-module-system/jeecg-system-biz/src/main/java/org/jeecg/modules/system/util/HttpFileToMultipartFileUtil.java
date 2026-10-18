package org.jeecg.modules.system.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.jeecg.common.util.FileDownloadUtils;
import org.jeecg.common.util.MyCommonsMultipartFile;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Description: http文件转MultipartFile
 * @author: wangshuai
 * @date: 2025/11/5 17:55
 */
public class HttpFileToMultipartFileUtil {

    /**
     * 获取
     *
     * @param fileUrl
     * @param filename
     * @return
     * @throws Exception
     */
    public static MultipartFile httpFileToMultipartFile(String fileUrl, String filename) throws Exception {
        //update-begin---author:liusq ---date:2026-06-29  for：【issues/9725】修复SSRF重定向绕过漏洞(CWE-918)，改用禁止自动跳转并逐跳SSRF校验的安全下载-----------
        // 原实现仅在下载前对 fileUrl 做一次 SSRF 校验，而 HttpURLConnection 默认自动跟随 3xx 重定向且不再复检，
        // 攻击者可用 302 跳转到 127.0.0.1 / 169.254.169.254 等内网或云元数据地址绕过校验。
        // 改用 FileDownloadUtils.download2BytesFromNet，其内部禁止自动跳转并对初始URL及每一跳重定向目标都做SSRF校验。
        byte[] bytes = FileDownloadUtils.download2BytesFromNet(fileUrl);
        //update-end---author:liusq ---date:2026-06-29  for：【issues/9725】修复SSRF重定向绕过漏洞(CWE-918)，改用禁止自动跳转并逐跳SSRF校验的安全下载-----------
        return convertByteToMultipartFile(bytes, filename);
    }

    /**
     * 下载图片数据
     */
    private static byte[] downloadImageData(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        connection.setRequestProperty("Accept", "image/*");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP请求失败，响应码: " + responseCode);
        }

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * byte转 MultipartFile
     *
     * @param data
     * @param fileName
     * @return
     */
    private static MultipartFile convertByteToMultipartFile(byte[] data, String fileName) {
        FileItemFactory factory = new DiskFileItemFactory();
        FileItem item = factory.createItem(fileName, "application/octet-stream", true, fileName);

        try (OutputStream os = item.getOutputStream();
             ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("字节数组转换失败", e);
        }

        try {
            return new MyCommonsMultipartFile(item);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}