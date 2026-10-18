package org.jeecg.modules.system.service.impl;

import org.jeecg.modules.system.entity.SysUgroup;
import org.jeecg.modules.system.entity.SysUgroupUser;
import org.jeecg.modules.system.mapper.SysUgroupMapper;
import org.jeecg.modules.system.service.ISysUgroupService;
import org.jeecg.modules.system.service.ISysUgroupUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * @Description: 用户组表
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Service("sysUgroupServiceImpl")
public class SysUgroupServiceImpl extends ServiceImpl<SysUgroupMapper, SysUgroup> implements ISysUgroupService {

    @Autowired
    private ISysUgroupUserService sysUgroupUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        this.baseMapper.deleteById(id);
        sysUgroupUserService.remove(new QueryWrapper<SysUgroupUser>().eq("group_id", id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<String> list) {
        this.baseMapper.deleteBatchIds(list);
        sysUgroupUserService.remove(new QueryWrapper<SysUgroupUser>().in("group_id", list));
    }
}
