package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysUgroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 用户组表
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
public interface ISysUgroupService extends IService<SysUgroup> {

    void deleteById(String id);

    void deleteByIds(List<String> list);
}
