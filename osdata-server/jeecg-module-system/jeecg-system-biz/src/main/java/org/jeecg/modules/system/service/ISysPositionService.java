package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.vo.SysPositionVO;

import java.util.List;

/**
 * @Description: 职务表
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
public interface ISysPositionService extends IService<SysPosition> {

    /**
     * 通过code查询
     * @param code 职务编码
     * @return SysPosition
     */
    SysPosition getByCode(String code);

    /**
     * 通过用户id获取职位名称列表
     * @param userId
     * @return
     */
    List<SysPosition> getPositionList(String userId);

    /**
     * 获取职位名称
     * @param postList
     * @return
     */
    String getPositionName(List<String> postList);

    /**
     * 批量通过用户id列表查询职位VO（含userId字段，用于批量同步场景消除N+1查询）
     *
     * @param userIds 用户id列表
     * @return 职位VO列表（每条记录含userId字段，供调用方分组）
     */
    List<SysPositionVO> getPositionListByUserIds(List<String> userIds);

}
