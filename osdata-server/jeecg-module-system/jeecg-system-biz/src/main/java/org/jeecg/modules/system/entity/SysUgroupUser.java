package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 用户组关系表
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Data
@TableName("sys_ugroup_user")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="用户组关系表")
public class SysUgroupUser implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键id")
    private java.lang.String id;
	/**用户id*/
	@Excel(name = "用户id", width = 15)
    @Schema(description = "用户id")
    private java.lang.String userId;
	/**用户组id*/
	@Excel(name = "用户组id", width = 15)
    @Schema(description = "用户组id")
    private java.lang.String groupId;
	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private java.lang.Integer tenantId;

    public SysUgroupUser() {
    }

    public SysUgroupUser(String userId, String groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }
}
