package org.jeecg.modules.airag.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.vo.AiArticleWriteVersionVo;
import java.util.List;

/**
 * @Description: AI应用
 * @Author: jeecg-boot
 * @Date:   2025-02-26
 * @Version: V1.0
 */
public interface IAiragAppService extends IService<AiragApp> {

    /**
     * 发布/取消发布应用，并维护分享令牌
     *
     * @param id 应用ID
     * @param release true=发布，false=取消发布
     * @return 发布成功时返回 shareToken；取消发布返回 null
     * @author scott
     * @since 2026-07-21 【issues/9787】应用级分享令牌
     */
    String releaseApp(String id, boolean release);

	/**
	 * 复制应用
	 *
	 * @param id 原应用ID
	 * @param currentTenantId 当前租户ID
	 * @return 新应用ID
	 * @author scott
	 * @since 2026-08-06 【LHZP-1512】AI应用增加复制功能
	 */
	String copyApp(String id, String currentTenantId);

    /**
     * 生成提示词
     * @param prompt
     * @return blocking 是否阻塞
     * @return
     * @author chenrui
     * @date 2025/3/12 14:45
     */
    Object generatePrompt(String prompt,boolean blocking);

    /**
     * 根据应用id生成提示词
     *
     * @param variables
     * @param memoryId
     * @param blocking
     * @return
     */
    Object generateMemoryByAppId(String variables, String memoryId, boolean blocking);

    /**
     * 写作保存
     * 
     * @param aiWriteVersionVo
     */
    void saveArticleWrite(AiArticleWriteVersionVo aiWriteVersionVo);

    /**
     * 写作列表
     * 
     * @return
     */
    List<AiArticleWriteVersionVo> listArticleWrite();

    /**
     * 写作删除
     * 
     * @param version
     */
    void deleteArticleWrite(String version);

}
