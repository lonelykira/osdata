package org.jeecg.modules.system.service.impl;

import org.jeecg.modules.system.entity.SysUgroupUser;
import org.jeecg.modules.system.mapper.SysUgroupUserMapper;
import org.jeecg.modules.system.service.ISysUgroupUserService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 用户组关系表
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Service("sysUgroupUserServiceImpl")
public class SysUgroupUserServiceImpl extends ServiceImpl<SysUgroupUserMapper, SysUgroupUser> implements ISysUgroupUserService {

}
