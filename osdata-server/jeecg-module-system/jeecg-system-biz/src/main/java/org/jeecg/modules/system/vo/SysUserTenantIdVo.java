package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * 用户-租户ID映射VO，用于批量查询用户所属租户（消除N+1查询）
 * @author scott
 * @since 2026-07-01 listAll接口N+1查询改造
 */
@Data
public class SysUserTenantIdVo {
    private String userId;
    private Integer tenantId;
}
