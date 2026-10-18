package org.jeecg.modules.airag.prompts.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.airag.prompts.entity.AiragPrompts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: airag_prompts
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
public interface AiragPromptsMapper extends BaseMapper<AiragPrompts> {

    //update-begin---author:chenrui ---date:2026-04-07  for：【QQYUN-14643】实现回收站取回和彻底删除-----------
    /**
     * 查询回收站分页列表（del_flag=1），绕过 @TableLogic 自动过滤
     */
    @Select("SELECT * FROM airag_prompts WHERE del_flag = 1")
    IPage<AiragPrompts> selectRecycleBinPage(IPage<AiragPrompts> page);

    /**
     * 从回收站取回（将 del_flag 置为 0），绕过 @TableLogic 自动过滤
     */
    void revertRecycleBin(@Param("ids") List<String> ids);

    /**
     * 从回收站彻底删除（物理删除），绕过 @TableLogic 自动过滤
     */
    void deleteRecycleBin(@Param("ids") List<String> ids);
    //update-end---author:chenrui ---date:2026-04-07  for：【QQYUN-14643】实现回收站取回和彻底删除-----------

}
