package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysFillRule;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 填值规则
 * @Author: jeecg-boot
 * @Date: 2019-11-07
 * @Version: V1.0
 */
public interface ISysFillRuleService extends IService<SysFillRule> {

    /**
     * 导入填值规则，并校验规则编码是否重复。
     *
     * @param file 导入文件
     * @param params 导入参数
     * @return 导入结果
     * @throws Exception 导入异常
     * @author wangshuai
     * @since 2026-08-03 LHZP-1265
     */
    Result<?> importExcelCheckRuleCode(MultipartFile file, ImportParams params) throws Exception;
}
