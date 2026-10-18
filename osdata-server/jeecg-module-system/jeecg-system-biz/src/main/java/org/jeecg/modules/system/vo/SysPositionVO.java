package org.jeecg.modules.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.system.entity.SysPosition;

/**
 * 职务VO，扩展了userId字段，用于批量查询职位时携带用户ID（供全量同步批量预加载场景）
 *
 * @author sjlei
 * @version V1.0
 * @date 2026-04-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPositionVO extends SysPosition {

    /**
     * 批量查询时携带的用户ID（非数据库字段，仅用于查询结果分组）
     */
    private String userId;

}
