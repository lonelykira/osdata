package org.jeecg.modules.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 用户组vo
 * @author: jeecg-boot
 */
@Data
public class SysUserGroupVO implements Serializable{
	private static final long serialVersionUID = 1L;

	/**用户组id*/
	private String groupId;
	/**对应的用户id集合*/
	private List<String> userIdList;

	public SysUserGroupVO() {
		super();
	}

	public SysUserGroupVO(String groupId, List<String> userIdList) {
		super();
		this.groupId = groupId;
		this.userIdList = userIdList;
	}

}
