package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUserDepPost;

import java.util.List;

import org.jeecg.modules.system.vo.SysUserDepPostVo;

/**
 * @Description: 部门岗位用户关联表 Mapper
 * @author: wangshuai
 * @date: 2025/9/5 12:01
 */
public interface SysUserDepPostMapper extends BaseMapper<SysUserDepPost> {

    /**
     * 通过用户id查询部门岗位用户
     *
     * @param userId
     * @return
     */
    @Select("select dep_id from sys_user_dep_post where user_id = #{userId}")
    List<String> getDepPostByUserId(@Param("userId") String userId);

    /**
     * 批量查询多个用户的兼职部门ID（含用户ID映射，跨数据库兼容，消除N+1查询）
     * @author scott
     * @since 2026-07-01 listAll接口N+1查询改造
     * @param userIds 用户ID列表
     * @return 每条含 userId 和 depId
     */
    List<SysUserDepPostVo> getDepPostByUserIds(@Param("userIds") List<String> userIds);
}
