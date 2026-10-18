package org.jeecg.modules.airag.llm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.mapper.AiragModelMapper;
import org.jeecg.modules.airag.llm.service.IAiragModelService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: AiRag模型配置
 * @Author: jeecg-boot
 * @Date: 2025-02-14
 * @Version: V1.0
 */
@Service
public class AiragModelServiceImpl extends ServiceImpl<AiragModelMapper, AiragModel> implements IAiragModelService {

	private static final String COPY_NAME_SUFFIX = "-复制";
	private static final int INACTIVE_FLAG = 0;

	/**
	 * {@inheritDoc}
	 *
	 * @author scott
	 * @since 2026-08-06 LHZP-1552 AI模型配置增加复制功能
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyModel(String id) {
		AiragModel sourceModel = this.getById(id);
		AssertUtils.assertNotEmpty("模型不存在", sourceModel);
		AiragModel copiedModel = new AiragModel();
		BeanUtils.copyProperties(sourceModel, copiedModel, "id", "createBy", "createTime", "updateBy", "updateTime", "sysOrgCode", "tenantId");
		copiedModel.setName(sourceModel.getName() + COPY_NAME_SUFFIX);
		copiedModel.setActivateFlag(INACTIVE_FLAG);
		this.save(copiedModel);
	}

}