package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysCheckRule;
import org.jeecg.modules.system.mapper.SysCheckRuleMapper;
import org.jeecg.modules.system.service.ISysCheckRuleService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description: 编码校验规则
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysCheckRuleServiceImpl extends ServiceImpl<SysCheckRuleMapper, SysCheckRule> implements ISysCheckRuleService {

    /**
     * 位数特殊符号，用于检查整个值，而不是裁剪某一段
     */
    private final String CHECK_ALL_SYMBOL = "*";

    @Override
    public SysCheckRule getByCode(String ruleCode) {
        LambdaQueryWrapper<SysCheckRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysCheckRule::getRuleCode, ruleCode);
        return super.getOne(queryWrapper);
    }

    @Override
    public Result<?> importExcel(MultipartFile file, ImportParams params) throws Exception {
        List<SysCheckRule> checkRules;
        try (InputStream inputStream = file.getInputStream()) {
            checkRules = ExcelImportUtil.importExcel(inputStream, SysCheckRule.class, params);
        }
        //update-begin---wangshuai---date:20260810  for：【LHZP-1278】【系统校验规则】导入 有重复 报错------------
        List<String> errorMessages = new ArrayList<>();
        int successLines = 0;
        for (int i = 0; i < checkRules.size(); i++) {
            SysCheckRule checkRule = checkRules.get(i);
            try {
                resetImportAuditFields(checkRule);
                if (isRuleCodeExists(checkRule.getRuleCode())) {
                    errorMessages.add("第" + (i + 1) + "行：规则Code[" + checkRule.getRuleCode() + "]已存在，忽略导入。");
                    continue;
                }
                if (!save(checkRule)) {
                    throw new JeecgBootException("数据保存失败");
                }
                successLines++;
            } catch (Exception e) {
                errorMessages.add("第" + (i + 1) + "行：" + e.getMessage());
                log.error("导入系统校验规则失败，行号：{}", i + 1, e);
            }
        }
        return ImportExcelUtil.imporReturnRes(errorMessages.size(), successLines, errorMessages);
        //update-end---author:wangshuai ---date:20260810  for：【LHZP-1278】【系统校验规则】导入 有重复 报错------------
    }

    /**
     * 判断规则Code是否已经存在。
     *
     * @param ruleCode 规则Code
     * @return 是否存在
     */
    private boolean isRuleCodeExists(String ruleCode) {
        return count(new LambdaQueryWrapper<SysCheckRule>().eq(SysCheckRule::getRuleCode, ruleCode)) > 0;
    }

    /**
     * 清空 Excel 中的审计字段，由 MyBatis 插入拦截器按当前用户重新填充。
     */
    private void resetImportAuditFields(SysCheckRule checkRule) {
        checkRule.setCreateBy(null);
        checkRule.setCreateTime(null);
        checkRule.setUpdateBy(null);
        checkRule.setUpdateTime(null);
    }

    /**
     * 通过用户设定的自定义校验规则校验传入的值
     *
     * @param checkRule
     * @param value
     * @return 返回 null代表通过校验，否则就是返回的错误提示文本
     */
    @Override
    public JSONObject checkValue(SysCheckRule checkRule, String value) {
        if (checkRule != null && StringUtils.isNotBlank(value)) {
            String ruleJson = checkRule.getRuleJson();
            if (StringUtils.isNotBlank(ruleJson)) {
                // 开始截取的下标，根据规则的顺序递增，但是 * 号不计入递增范围
                int beginIndex = 0;
                JSONArray rules = JSON.parseArray(ruleJson);
                for (int i = 0; i < rules.size(); i++) {
                    JSONObject result = new JSONObject();
                    JSONObject rule = rules.getJSONObject(i);
                    // 位数
                    String digits = rule.getString("digits");
                    result.put("digits", digits);
                    // 验证规则
                    String pattern = rule.getString("pattern");
                    result.put("pattern", pattern);
                    // 未通过时的提示文本
                    String message = rule.getString("message");
                    result.put("message", message);

                    // 根据用户设定的区间，截取字符串进行验证
                    String checkValue;
                    // 是否检查整个值而不截取
                    if (CHECK_ALL_SYMBOL.equals(digits)) {
                        checkValue = value;
                    } else {
                        int num = Integer.parseInt(digits);
                        int endIndex = beginIndex + num;
                        // 如果结束下标大于给定的值的长度，则取到最后一位
                        endIndex = endIndex > value.length() ? value.length() : endIndex;
                        // 如果开始下标大于结束下标，则说明用户还尚未输入到该位置，直接赋空值
                        if (beginIndex > endIndex) {
                            checkValue = "";
                        } else {
                            checkValue = value.substring(beginIndex, endIndex);
                        }
                        result.put("beginIndex", beginIndex);
                        result.put("endIndex", endIndex);
                        beginIndex += num;
                    }
                    result.put("checkValue", checkValue);
                    boolean passed = Pattern.matches(pattern, checkValue);
                    result.put("passed", passed);
                    // 如果没有通过校验就返回错误信息
                    if (!passed) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

}
