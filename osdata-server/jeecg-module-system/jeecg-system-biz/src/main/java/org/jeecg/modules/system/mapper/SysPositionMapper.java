package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.vo.SysPositionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 职务表
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
public interface SysPositionMapper extends BaseMapper<SysPosition> {

    /**
     * 通过用户id获取职位名称
     * @param userId
     * @return
     */
    List<SysPosition> getPositionList(@Param("userId") String userId);

    /**
     * 通过职位id获取职位名称
     * @param postList
     * @return
     */
    List<SysPosition> getPositionName(@Param("postList") List<String> postList);

    /**
     * 根据职位名称获取职位id
     * @param name
     * @return
     */
    @Select("SELECT id FROM sys_position WHERE name = #{name} AND tenant_id = #{tenantId} ORDER BY create_time DESC")
    List<String> getPositionIdByName(@Param("name") String name, @Param("tenantId") Integer tenantId, @Param("page") Page<SysPosition> page);

    /**
     * 批量通过用户id列表查询职位（含userId字段，用于批量同步场景）
     *
     * @param userIds 用户id列表
     * @return 职位VO列表（每条记录含userId字段，供调用方分组）
     */
    List<SysPositionVO> getPositionListByUserIds(@Param("userIds") List<String> userIds);

}
