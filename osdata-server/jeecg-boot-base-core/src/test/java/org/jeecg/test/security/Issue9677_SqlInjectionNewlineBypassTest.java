package org.jeecg.test.security;

import org.jeecg.common.exception.JeecgSqlInjectionException;
import org.jeecg.common.util.SqlInjectionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 【issue/9677】SQL注入换行符绕过修复 — 单元测试
 *
 * 漏洞：攻击者用换行符(\n)替代空格，绕过关键词黑名单和正则检测
 * 例如 "1\nand\nsleep(2)" 绕过 "and " 和 "sleep\\s*\\(" 的检测
 *
 * 修复：在所有检测入口对输入做 \\s+ → 空格 的归一化，正则加 (?s) DOTALL 模式
 *
 * @author wangshuai
 * @date 2026-06-16
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9677_SqlInjectionNewlineBypassTest {

    // ========== 漏洞一：SQL注入换行绕过 ==========

    @Nested
    @DisplayName("filterContent — 换行绕过检测")
    class FilterContentNewlineBypass {

        @Test
        @DisplayName("正常空格形式 'and sleep(2)' 被拦截（回归）")
        void normalSpaceBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1 and sleep(2)", null));
        }

        @Test
        @DisplayName("换行符形式 '1\\nand\\nsleep(2)' 应被拦截")
        void newlineAndSleepBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nand\nsleep(2)", null));
        }

        @Test
        @DisplayName("制表符形式 '1\\tand\\tsleep(2)' 应被拦截")
        void tabAndSleepBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\tand\tsleep(2)", null));
        }

        @Test
        @DisplayName("回车换行混合 '1\\r\\nand\\r\\nsleep(2)' 应被拦截")
        void crlfAndSleepBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\r\nand\r\nsleep(2)", null));
        }

        @Test
        @DisplayName("换行符形式 '1\\nselect\\n1' 应被拦截")
        void newlineSelectBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nselect\n1", null));
        }

        @Test
        @DisplayName("换行符形式 '1\\nor\\n1=1' 应被拦截")
        void newlineOrBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nor\n1=1", null));
        }

        @Test
        @DisplayName("换行符+正则函数 'benchmark\\n(1000,md5(1))' 应被拦截")
        void newlineBenchmarkBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nand\nbenchmark\n(1000,md5(1))", null));
        }

        @Test
        @DisplayName("换行符+show tables应被拦截")
        void newlineShowTablesBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nand\nshow\ntables", null));
        }
    }

    @Nested
    @DisplayName("specialFilterContentForDictSql — 换行绕过检测")
    class DictSqlNewlineBypass {

        @Test
        @DisplayName("正常空格形式 'and sleep(5)' 被拦截（回归）")
        void normalSpaceBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and sleep(5)"));
        }

        @Test
        @DisplayName("换行符形式 '1\\nand\\nsleep(5)' 应被拦截")
        void newlineSleepBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\nand\nsleep(5)"));
        }

        @Test
        @DisplayName("换行符+select 绕过应被拦截")
        void newlineSelectBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\nunion\nselect\npassword\nfrom\nsys_user"));
        }

        @Test
        @DisplayName("换行符+database()应被拦截")
        void newlineDatabaseFuncBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\nand\ndatabase()='jeecg'"));
        }

        @Test
        @DisplayName("制表符+extractvalue应被拦截")
        void tabExtractvalueBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\tand\textractvalue(1,concat(0x7e))"));
        }

        @Test
        @DisplayName("多种空白混合应被拦截")
        void mixedWhitespaceBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\n\tand \nsleep(5)"));
        }
    }

    @Nested
    @DisplayName("specialFilterContentForOnlineReport — 换行绕过检测")
    class OnlineReportNewlineBypass {

        @Test
        @DisplayName("换行符+insert绕过应被拦截")
        void newlineInsertBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForOnlineReport("1\ninsert\ninto\nsys_user"));
        }

        @Test
        @DisplayName("换行符+delete绕过应被拦截")
        void newlineDeleteBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForOnlineReport("1\ndelete\nfrom\nsys_user"));
        }

        @Test
        @DisplayName("换行符+drop绕过应被拦截")
        void newlineDropBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForOnlineReport("1;\ndrop\ntable\nsys_user"));
        }
    }

    // ========== 合法输入不被误拦截 ==========

    @Nested
    @DisplayName("合法输入回归 — 确保不误拦截")
    class LegitimateInputs {

        @Test
        @DisplayName("简单条件通过")
        void simpleConditionPasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.filterContent("status=1", null));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("status=1"));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForOnlineReport("status=1"));
        }

        @Test
        @DisplayName("含引号的字符串值通过")
        void quotedValuePasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("dept_id='10001'"));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("create_time > '2026-01-01'"));
        }

        @Test
        @DisplayName("空/null值通过")
        void blankPasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.filterContent((String) "", null));
            assertDoesNotThrow(() -> SqlInjectionUtil.filterContent((String) null, null));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql(""));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql(null));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForOnlineReport(""));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForOnlineReport(null));
        }

        @Test
        @DisplayName("字典条件中含like的合法查询通过")
        void likePasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("name like '%张%'"));
        }

        @Test
        @DisplayName("含数字比较的条件通过")
        void numericComparisonPasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("age >= 18"));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("level != 0"));
        }
    }

    // ========== 既有攻击回归 ==========

    @Nested
    @DisplayName("既有攻击向量回归 — 确保已有修复不被破坏")
    class ExistingAttackRegression {

        @Test
        @DisplayName("【#9523】时间盲注 sleep/benchmark/pg_sleep/waitfor delay 仍拦截")
        void timeBlindStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and sleep(5)"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and benchmark(1000000,md5(1))"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and pg_sleep(5)"));
        }

        @Test
        @DisplayName("【#9524】(extractvalue/(updatexml 报错注入仍拦截")
        void errorBasedStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql(
                            "id=1 and (updatexml(1,concat(0x7e,(user())),1))"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql(
                            "id=1 and (extractvalue(1,concat(0x7e,(user()))))"));
        }

        @Test
        @DisplayName("【#9571】database()/version()/ascii() 信息泄露函数仍拦截")
        void booleanBlindStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and database()='jeecg-boot'"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and version() like '8%'"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=ascii('a')"));
        }

        @Test
        @DisplayName("【#9572】select(/insert(/delete( 紧贴形式仍拦截")
        void keywordParenStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql(
                            "id=(select(id)from(sys_user)where(username='admin'))"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("x=insert(1,2,3,'a')"));
        }

        @Test
        @DisplayName("SQL注释 -- 和 /**/ 仍拦截")
        void sqlCommentStillBlocked() {
            assertThrows(RuntimeException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1-- "));
            assertThrows(RuntimeException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 /*comment*/"));
        }

        @Test
        @DisplayName("information_schema 仍拦截")
        void infoSchemaStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id in (select 1 from information_schema.tables)"));
        }
    }
}
