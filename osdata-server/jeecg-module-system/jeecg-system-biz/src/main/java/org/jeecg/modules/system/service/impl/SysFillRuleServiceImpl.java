package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysFillRule;
import org.jeecg.modules.system.mapper.SysFillRuleMapper;
import org.jeecg.modules.system.service.ISysFillRuleService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 填值规则
 * @Author: jeecg-boot
 * @Date: 2019-11-07
 * @Version: V1.0
 */
@Service("sysFillRuleServiceImpl")
public class SysFillRuleServiceImpl extends ServiceImpl<SysFillRuleMapper, SysFillRule> implements ISysFillRuleService {

    private static final String UNIQUE_RULE_CODE_INDEX = "uk_sfr_rule_code";

    /**
     * 导入填值规则，并返回重复规则编码所在的Excel行号。
     *
     * @param file 导入文件
     * @param params 导入参数
     * @return 导入结果
     * @throws Exception 导入异常
     * @author wangshuai
     * @since 2026-08-03 LHZP-1265
     */
    @Override
    public Result<?> importExcelCheckRuleCode(MultipartFile file, ImportParams params) throws Exception {
        List<SysFillRule> fillRules;
        try (InputStream inputStream = file.getInputStream()) {
            fillRules = ExcelImportUtil.importExcel(inputStream, SysFillRule.class, params);
        }
        List<String> errorMessages = new ArrayList<>();
        int successLines = 0;
        for (int i = 0; i < fillRules.size(); i++) {
            SysFillRule fillRule = fillRules.get(i);
            //update-begin---author:wangshuai---date:20260807---for:规则导入时审计字段使用当前用户和当前时间---
            resetImportAuditFields(fillRule);
            //update-end---author:wangshuai---date:20260807---for:规则导入时审计字段使用当前用户和当前时间---
            int excelRowNumber = params.getTitleRows() + params.getHeadRows() + i + 1;
            if (isRuleCodeExists(fillRule.getRuleCode())) {
                errorMessages.add("Excel第" + excelRowNumber + "行：规则编码【" + fillRule.getRuleCode() + "】重复，忽略导入。");
                continue;
            }
            try {
                if (save(fillRule)) {
                    successLines++;
                } else {
                    errorMessages.add("Excel第" + excelRowNumber + "行：数据保存失败，忽略导入。");
                }
            } catch (Exception e) {
                if (isDuplicateRuleCodeException(e)) {
                    errorMessages.add("Excel第" + excelRowNumber + "行：规则编码【" + fillRule.getRuleCode() + "】重复，忽略导入。");
                } else {
                    throw e;
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorMessages.size(), successLines, errorMessages);
    }

    /**
     * 清空 Excel 中的审计字段，由 MyBatis 插入拦截器按当前用户重新填充。
     */
    private void resetImportAuditFields(SysFillRule fillRule) {
        fillRule.setCreateBy(null);
        fillRule.setCreateTime(null);
        fillRule.setUpdateBy(null);
        fillRule.setUpdateTime(null);
    }

    /**
     * 判断规则编码是否已经存在。
     *
     * @param ruleCode 规则编码
     * @return 是否存在
     * @author wangshuai
     * @since 2026-08-03 LHZP-1265
     */
    private boolean isRuleCodeExists(String ruleCode) {
        return ruleCode != null && count(Wrappers.<SysFillRule>lambdaQuery().eq(SysFillRule::getRuleCode, ruleCode)) > 0;
    }

    /**
     * 判断异常链中是否包含规则编码唯一索引冲突。
     *
     * @param exception 保存数据时的异常
     * @return 是否为规则编码重复异常
     * @author wangshuai
     * @since 2026-08-03 LHZP-1265
     */
    private boolean isDuplicateRuleCodeException(Exception exception) {
        Throwable cause = exception;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.toLowerCase().contains(UNIQUE_RULE_CODE_INDEX)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
