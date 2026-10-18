package org.jeecg.modules.message.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecg.modules.message.mapper.SysMessageTemplateMapper;
import org.jeecg.modules.message.service.ISysMessageTemplateService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 消息模板
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
@Service
public class SysMessageTemplateServiceImpl extends JeecgServiceImpl<SysMessageTemplateMapper, SysMessageTemplate> implements ISysMessageTemplateService {

    @Autowired
    private SysMessageTemplateMapper sysMessageTemplateMapper;


    @Override
    public List<SysMessageTemplate> selectByCode(String code) {
        return sysMessageTemplateMapper.selectByCode(code);
    }

	@Override
	public Result<?> importExcelCheckTemplateCode(MultipartFile file, ImportParams params) throws Exception {
		List<SysMessageTemplate> messageTemplates;
		try (InputStream inputStream = file.getInputStream()) {
			messageTemplates = ExcelImportUtil.importExcel(inputStream, SysMessageTemplate.class, params);
		}
		List<String> errorMessages = new ArrayList<>();
		int successLines = 0;
		for (int i = 0; i < messageTemplates.size(); i++) {
			SysMessageTemplate messageTemplate = messageTemplates.get(i);
			messageTemplate.setCreateBy(null);
			messageTemplate.setCreateTime(null);
			messageTemplate.setUpdateTime(null);
            messageTemplate.setUpdateBy(null);
			int excelRowNumber = params.getTitleRows() + params.getHeadRows() + i + 1;
			if (isTemplateCodeExists(messageTemplate.getTemplateCode())) {
				errorMessages.add(excelRowNumber + "_模板编码【" + messageTemplate.getTemplateCode() + "】重复，忽略导入。");
				continue;
			}
			try {
				if (save(messageTemplate)) {
					successLines++;
				} else {
					errorMessages.add(excelRowNumber + "_数据保存失败，忽略导入。");
				}
			} catch (Exception e) {
				if (isDuplicateKeyException(e)) {
					errorMessages.add(excelRowNumber + "_模板编码【" + messageTemplate.getTemplateCode() + "】重复，忽略导入。");
				} else {
					throw e;
				}
			}
		}
		return ImportExcelUtil.imporReturnRes(errorMessages.size(), successLines, errorMessages);
	}

	private boolean isTemplateCodeExists(String templateCode) {
		return templateCode != null && count(Wrappers.<SysMessageTemplate>lambdaQuery()
			.eq(SysMessageTemplate::getTemplateCode, templateCode)) > 0;
	}

	private boolean isDuplicateKeyException(Exception exception) {
		Throwable cause = exception;
		while (cause != null) {
			if (cause instanceof DuplicateKeyException) {
				return true;
			}
			cause = cause.getCause();
		}
		return false;
	}
}
