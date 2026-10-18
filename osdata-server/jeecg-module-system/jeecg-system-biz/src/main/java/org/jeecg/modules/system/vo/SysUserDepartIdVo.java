package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * 用户-部门ID映射VO，用于批量查询用户所属部门（消除N+1查询）
 * @author scott
 * @since 2026-07-01 listAll接口N+1查询改造
 */
@Data
public class SysUserDepartIdVo {
    private String userId;
    private String departId;
}
