package org.jeecg.modules.message.service;

import java.util.List;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 消息模板
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
public interface ISysMessageTemplateService extends JeecgService<SysMessageTemplate> {

    /**
     * 通过模板CODE查询消息模板
     * @param code 模板CODE
     * @return
     */
    List<SysMessageTemplate> selectByCode(String code);

	/**
	 * 导入消息模板并校验模板编码。
	 *
	 * @param file 导入文件
	 * @param params 导入参数
	 * @return 导入结果
	 * @throws Exception 导入异常
	 */
	Result<?> importExcelCheckTemplateCode(MultipartFile file, ImportParams params) throws Exception;
}
