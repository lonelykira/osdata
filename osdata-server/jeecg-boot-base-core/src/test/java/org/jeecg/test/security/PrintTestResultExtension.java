package org.jeecg.test.security;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;

/**
 * 通用测试结果打印扩展：每个用例执行结束后自动打印一行结果，
 * 解决"测试通过时控制台静默、看起来像没跑"的困惑。
 *
 * 用法：在测试类上加 @ExtendWith(PrintTestResultExtension.class)。
 * 输出形如： [PASS] 发生重定向时，初始 URL 与重定向目标都各被 checkSsrfHttpUrl 校验一次
 *
 * 注：状态标记用 ASCII，避免 Windows GBK 控制台乱码；用例名（@DisplayName 中文）
 * 在 IDE 的 UTF-8 控制台可正常显示。
 *
 * @author wangshuai
 * @date 2026-06-17
 */
public class PrintTestResultExtension implements TestWatcher {

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println("  [PASS] " + context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        System.out.println("  [FAIL] " + context.getDisplayName()
                + "  -> " + (cause == null ? "" : cause.getMessage()));
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        System.out.println("  [ABORTED] " + context.getDisplayName());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println("  [SKIP] " + context.getDisplayName()
                + reason.map(r -> "  (" + r + ")").orElse(""));
    }
}
