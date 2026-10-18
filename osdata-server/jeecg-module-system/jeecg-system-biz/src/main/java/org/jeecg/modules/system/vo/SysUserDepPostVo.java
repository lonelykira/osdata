package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * 用户-兼职部门岗位ID映射VO，用于批量查询用户兼职岗位（消除N+1查询）
 * @author scott
 * @since 2026-07-01 listAll接口N+1查询改造
 */
@Data
public class SysUserDepPostVo {
    private String userId;
    private String depId;
}
