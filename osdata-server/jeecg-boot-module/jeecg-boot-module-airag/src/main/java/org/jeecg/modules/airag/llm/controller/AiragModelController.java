package org.jeecg.modules.airag.llm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.ai.factory.AiModelFactory;
import org.jeecg.ai.factory.AiModelOptions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.airag.app.enums.ImageEditEnum;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.handler.AIChatHandler;
import org.jeecg.modules.airag.llm.handler.AiragModelTestParamsResolver;
import org.jeecg.modules.airag.llm.handler.EmbeddingHandler;
import org.jeecg.modules.airag.llm.service.IAiragModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * @Description: AiRag模型配置
 * @Author: jeecg-boot
 * @Date: 2025-02-14
 * @Version: V1.0
 */
@Tag(name = "AiRag模型配置")
@RestController
@RequestMapping("/airag/airagModel")
@Slf4j
public class AiragModelController extends JeecgController<AiragModel, IAiragModelService> {
    @Autowired
    private IAiragModelService airagModelService;

    @Autowired
    AIChatHandler aiChatHandler;

    /**
     * 分页列表查询
     *
     * @param airagModel
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    @RequiresPermissions("airag:model:list")
    public Result<IPage<AiragModel>> queryPageList(AiragModel airagModel, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<AiragModel> queryWrapper = QueryGenerator.initQueryWrapper(airagModel, req.getParameterMap());
        Page<AiragModel> page = new Page<AiragModel>(pageNo, pageSize);
        IPage<AiragModel> pageList = airagModelService.page(page, queryWrapper);
        //update-begin---author:scott ---date:20260506  for：【issues/9600】低权限用户可获取 LLM API Key——列表场景禁止回传 credential，避免有 airag:model:list 的角色一次性拖走所有模型的密钥。编辑场景密钥仍由 /queryById 单条返回-----------
        if (pageList != null && pageList.getRecords() != null) {
            pageList.getRecords().forEach(m -> m.setCredential(null));
        }
        //update-end---author:scott ---date:20260506  for：【issues/9600】低权限用户可获取 LLM API Key——列表场景禁止回传 credential，避免有 airag:model:list 的角色一次性拖走所有模型的密钥。编辑场景密钥仍由 /queryById 单条返回-----------
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param airagModel
     * @return
     */
    @PostMapping(value = "/add")
    @RequiresPermissions("airag:model:add")
    public Result<String> add(@RequestBody AiragModel airagModel) {
        // 验证 模型名称/模型类型/基础模型
        AssertUtils.assertNotEmpty("模型名称不能为空", airagModel.getName());
        AssertUtils.assertNotEmpty("模型类型不能为空", airagModel.getModelType());
        AssertUtils.assertNotEmpty("基础模型不能为空", airagModel.getModelName());
        // 默认未激活
        if(oConvertUtils.isObjectEmpty(airagModel.getActivateFlag())){
            airagModel.setActivateFlag(0);
        }
        airagModelService.save(airagModel);
        return Result.OK("添加成功！");
    }

	/**
	 * 复制AI模型配置
	 *
	 * @param id 原模型ID
	 * @return 复制结果
	 * @author scott
	 * @since 2026-08-06 LHZP-1552 AI模型配置增加复制功能
	 */
	@PostMapping(value = "/copy/{id}")
	@RequiresPermissions("airag:model:add")
	public Result<String> copy(@PathVariable("id") String id) {
		airagModelService.copyModel(id);
		return Result.OK("复制成功！");
	}

    /**
     * 编辑
     *
     * @param airagModel
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("airag:model:edit")
    public Result<String> edit(@RequestBody AiragModel airagModel) {
        airagModelService.updateById(airagModel);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("airag:model:delete")
    public Result<String> delete(HttpServletRequest request, @RequestParam(name = "id", required = true) String id) {
        //update-begin---author:chenrui ---date:20250606  for：[issues/8337]关于ai工作列表的数据权限问题 #8337------------
        //如果是saas隔离的情况下，判断当前租户id是否是当前租户下的
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            AiragModel model = airagModelService.getById(id);
            //获取当前租户
            String currentTenantId = TokenUtils.getTenantIdByRequest(request);
            if (null == model || !model.getTenantId().equals(currentTenantId)) {
                return Result.error("删除AI模型失败，不能删除其他租户的AI模型！");
            }
        }
        //update-end---author:chenrui ---date:20250606  for：[issues/8337]关于ai工作列表的数据权限问题 #8337------------
        airagModelService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @RequiresPermissions("airag:model:queryById")
    public Result<AiragModel> queryById(@RequestParam(name = "id", required = true) String id) {
        AiragModel airagModel = airagModelService.getById(id);
        if (airagModel == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(airagModel);
    }

    //update-begin---author:scott ---date:20260506  for：【issues/9600】低权限用户可获取 LLM API Key——新增无权限的简化单条接口，返回模型基础信息但脱敏 credential，避免泄露 API Key-----------
    /**
     * 通过id查询（简化版，无需权限），返回模型基础信息但脱敏 credential
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/detail")
    public Result<AiragModel> queryByIdSimple(@RequestParam(name = "id", required = true) String id) {
        AiragModel airagModel = airagModelService.getById(id);
        if (airagModel == null) {
            return Result.error("未找到对应数据");
        }
        airagModel.setCredential(null);
        return Result.OK(airagModel);
    }
    //update-end---author:scott ---date:20260506  for：【issues/9600】低权限用户可获取 LLM API Key——新增无权限的简化单条接口，返回模型基础信息但脱敏 credential，避免泄露 API Key-----------

    /**
     * 导出excel
     *
     * @param request
     * @param airagModel
     */
    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("airag:model:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AiragModel airagModel) {
        return super.exportXls(request, airagModel, AiragModel.class, "AiRag模型配置");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @RequiresPermissions("airag:model:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AiragModel.class);
    }

    @PostMapping(value = "/test")
    @RequiresPermissions("airag:model:test")
    public Result<?> test(@RequestBody AiragModel airagModel) {
        // 验证 模型名称/模型类型/基础模型
        AssertUtils.assertNotEmpty("模型名称不能为空", airagModel.getName());
        AssertUtils.assertNotEmpty("模型类型不能为空", airagModel.getModelType());
        AssertUtils.assertNotEmpty("基础模型不能为空", airagModel.getModelName());
        //测试连接默认为已激活状态
        airagModel.setActivateFlag(1);
        try {
            if(LLMConsts.MODEL_TYPE_LLM.equals(airagModel.getModelType())){
                aiChatHandler.completions(airagModel, Collections.singletonList(UserMessage.from("To test whether it can be successfully called, simply return success")), null);
            }else if(LLMConsts.MODEL_TYPE_EMBED.equals(airagModel.getModelType())){
                AiModelOptions aiModelOptions = EmbeddingHandler.buildModelOptions(airagModel);
                EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(aiModelOptions);
                embeddingModel.embed("test text");
            //update-begin---author:wangshuai---date:2026-01-07---for:【QQYUN-12145】【AI】AI 绘画创作---=
            }else if(LLMConsts.MODEL_TYPE_IMAGE.equals(airagModel.getModelType())){
                AIChatParams aiChatParams = new AIChatParams();
				//update-begin---author:scott ---date:20260810  for：图片模型测试连接使用快速参数-----------
				AiragModelTestParamsResolver.applyImageTestParams(aiChatParams, airagModel.getProvider(), airagModel.getModelName());
				//update-end---author:scott ---date:20260810  for：图片模型测试连接使用快速参数-----------
                //update-begin---author:wangshuai---date:2026-03-02---for:兼容图生图模型测试---
                String modelName = airagModel.getModelName();
                if(ImageEditEnum.isImageEditModel(modelName)){
                    List<String> images = new ArrayList<>();
                    images.add("https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/jeecg_1772268161540.jpg");
                    aiChatHandler.imageEdit(airagModel, "Generate a picture of a cartoon cat", images,aiChatParams);
                }else{
                    aiChatHandler.imageGenerate(airagModel, "Generate a picture of a cartoon cat", aiChatParams);
                }
                //update-end---author:wangshuai---date:2026-03-02---for:兼容图生图模型测试---
            }
            //update-end---author:wangshuai---date:2026-01-07---for:【QQYUN-12145】【AI】AI 绘画创作---
        }catch (Exception e){
            log.error("测试模型连接失败", e);
            return Result.error(e.getMessage());
        }
        // 测试成功激活数据
        airagModel.setActivateFlag(1);
        airagModelService.updateById(airagModel);
        return Result.OK("");
    }

}
