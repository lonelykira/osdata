package org.jeecg.modules.airag.llm.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @Description: 命令行执行工具类
 * @Author: chenrui
 * @Date: 2024/4/8 10:11
 */
@Slf4j
public class CommandExecUtil {


    /**
     * 执行命令行
     *
     * @param command
     * @param args
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2024/4/9 10:59
     */
    public static String execCommand(String command, String[] args) throws IOException {
        if (null == command || command.isEmpty()) {
            throw new IllegalArgumentException("命令不能为空");
        }
        return execCommand(command.split(" "), args);
    }

    /**
     * 禁止在参数中出现的 Shell 元字符（适用于 Windows cmd 和 Unix shell）
     */
    private static final Pattern SHELL_INJECTION_PATTERN =
            Pattern.compile("[&|;<>`$!\\\\\\r\\n]");

    /**
     * 禁止文件名中出现的危险字符（防止通过文件名注入命令）
     */
    private static final Pattern FILENAME_INJECTION_PATTERN =
            Pattern.compile("[&|;<>`$!\"'\\r\\n]");

    /**
     * 校验单个命令参数，拒绝包含 Shell 注入字符的参数
     *
     * @param arg 待校验参数
     * @throws IllegalArgumentException 若参数包含危险字符
     */
    public static void validateArg(String arg) {
        if (arg != null && SHELL_INJECTION_PATTERN.matcher(arg).find()) {
            throw new IllegalArgumentException("命令参数包含非法字符，已拒绝执行: " + arg);
        }
    }

    /**
     * 校验文件路径，拒绝包含危险字符（防止文件名注入）
     *
     * @param filePath 待校验文件路径
     * @throws IllegalArgumentException 若文件路径包含危险字符
     */
    public static void validateFilePath(String filePath) {
        if (filePath != null && FILENAME_INJECTION_PATTERN.matcher(filePath).find()) {
            throw new IllegalArgumentException("文件路径包含非法字符，已拒绝处理: " + filePath);
        }
    }

    /**
     * 执行命令行
     *
     * @param command 脚本目录
     * @param args    参数
     * @author chenrui
     * @date 2024/4/09 10:30
     */
    public static String execCommand(String[] command, String[] args) throws IOException {

        if (null == command || command.length == 0) {
            throw new IllegalArgumentException("命令不能为空");
        }

        if (null != args && args.length > 0) {
            // 校验每一个用户可控的参数，防止命令注入
            for (String arg : args) {
                validateArg(arg);
            }
            command = (String[]) ArrayUtils.addAll(command, args);
        }

        // 直接使用 ProcessBuilder，不经过系统 Shell（防止 Shell 注入）
        // 注意：不再使用 cmd.exe /c 或 /bin/sh -c，参数数组由 JVM 直接传递给操作系统
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);

        Process process = null;
        try {
            log.debug(" =============================== Runtime command Script ===============================" );
            log.debug(String.join(" ", command));
            log.debug(" =============================== Runtime command Script =============================== " );
            process = Runtime.getRuntime().exec(command);
            try (ByteArrayOutputStream resultOutStream = new ByteArrayOutputStream();
                 InputStream processInStream = new BufferedInputStream(process.getInputStream())) {
                new Thread(new InputStreamRunnable(process.getErrorStream(), "ErrorStream")).start();
                int num;
                byte[] bs = new byte[1024];
                while ((num = processInStream.read(bs)) != -1) {
                    resultOutStream.write(bs, 0, num);
                    String stepMsg = new String(bs);
//                    log.debug("命令行日志:" + stepMsg);
                    if (stepMsg.contains("input any key to continue...")) {
                        process.destroy();
                    }
                }
                String result = resultOutStream.toString();
                log.debug("执行命令完成:" + result);
                return result;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * exec 控制台输出获取线程类
     * 使用单独的线程获取控制台输出,防止输入流阻塞
     *
     * @author chenrui
     * @date 2024/4/09 10:30
     */
    static class InputStreamRunnable implements Runnable {
        BufferedReader bReader = null;
        String type = null;

        public InputStreamRunnable(InputStream is, String _type) {
            try {
                bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), StandardCharsets.UTF_8));
                type = _type;
            } catch (Exception ex) {
            }
        }

        @SuppressWarnings("unused")
        public void run() {
            String line;
            int lineNum = 0;

            try {
                while ((line = bReader.readLine()) != null) {
                    lineNum++;
                    // Thread.sleep(200);
                }
                bReader.close();
            } catch (Exception ignored) {
            }
        }
    }
}
