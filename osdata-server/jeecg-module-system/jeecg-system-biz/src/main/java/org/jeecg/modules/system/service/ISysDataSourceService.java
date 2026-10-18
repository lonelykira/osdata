package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysDataSource;

import java.io.IOException;
import java.util.List;

/**
 * @Description: 多数据源管理
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
public interface ISysDataSourceService extends IService<SysDataSource> {

    /**
     * 添加数据源
     * @param sysDataSource
     * @return
     */
    Result saveDataSource(SysDataSource sysDataSource);

    /**
     * 修改数据源
     * @param sysDataSource
     * @return
     */
    Result editDataSource(SysDataSource sysDataSource);


    /**
     * 删除数据源
     * @param id
     * @return
     */
    Result deleteDataSource(String id);

    /**
     * 【QQYUN-15337 多数据源】确保指定编码的数据源已注册到 baomidou 动态数据源池（已注册则跳过）。
     * 用于在 push 数据源上下文前确保该数据源可用，主要给唯一性校验、复制等"未走 /online/cgform/api/** 请求"
     * 但又需要操作 online 表单物理表的入口使用。
     *
     * @param code sys_data_source.code
     */
    void ensureRegistered(String code);

    /**
     * 导入数据源，单行失败不影响其他数据。
     *
     * @param dataSources 待导入数据源
     * @return 导入统计结果
     */
    Result<?> importDataSources(List<SysDataSource> dataSources) throws IOException;
}
