package org.jeecg.modules.system.service.impl;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.druid.DruidDataSourceCreator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.jeecg.modules.system.entity.SysDataSource;
import org.jeecg.modules.system.mapper.SysDataSourceMapper;
import org.jeecg.modules.system.service.ISysDataSourceService;
import org.jeecg.modules.system.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 多数据源管理
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Slf4j
@Service
public class SysDataSourceServiceImpl extends ServiceImpl<SysDataSourceMapper, SysDataSource> implements ISysDataSourceService {

    @Autowired
    private DruidDataSourceCreator dataSourceCreator;

    @Autowired
    private DataSource dataSource;

    @Override
    public Result saveDataSource(SysDataSource sysDataSource) {
        try {
            long count = checkDbCode(sysDataSource.getCode());
            if (count > 0) {
                return Result.error("数据源编码已存在");
            }
            String dbPassword = sysDataSource.getDbPassword();
            if (StringUtils.isNotBlank(dbPassword)) {
                String encrypt = SecurityUtil.jiami(dbPassword);
                sysDataSource.setDbPassword(encrypt);
            }
            boolean result = save(sysDataSource);
            if (result) {
                //动态创建数据源
                //addDynamicDataSource(sysDataSource, dbPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.OK("添加成功！");
    }

    @Override
    public Result editDataSource(SysDataSource sysDataSource) {
        try {
            SysDataSource d = getById(sysDataSource.getId());
            DataSourceCachePool.removeCache(d.getCode());
            //update-begin---author:jeecg ---date:20260512  for：【QQYUN-15337】online表单支持多数据源，数据源编辑后移除已注册的动态数据源，下次使用时按新配置重新注册-----------
            removeDynamicDataSource(d.getCode());
            //update-end---author:jeecg ---date:20260512  for：【QQYUN-15337】online表单支持多数据源，数据源编辑后移除已注册的动态数据源，下次使用时按新配置重新注册-----------
            String dbPassword = sysDataSource.getDbPassword();
            if (StringUtils.isNotBlank(dbPassword)) {
                String encrypt = SecurityUtil.jiami(dbPassword);
                sysDataSource.setDbPassword(encrypt);
            } else {
                // 密码留空表示不修改，置 null 避免被 updateById 以空串覆盖原密码
                sysDataSource.setDbPassword(null);
            }
            Boolean result=updateById(sysDataSource);
            if(result){
                //先删除老的数据源
               // removeDynamicDataSource(d.getCode());
                //添加新的数据源
                //addDynamicDataSource(sysDataSource,dbPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.OK("编辑成功!");
    }

    @Override
    public Result deleteDataSource(String id) {
        SysDataSource sysDataSource = getById(id);
        DataSourceCachePool.removeCache(sysDataSource.getCode());
        //update-begin---author:jeecg ---date:20260512  for：【QQYUN-15337】online表单支持多数据源，数据源删除后同步移除已注册的动态数据源-----------
        removeDynamicDataSource(sysDataSource.getCode());
        //update-end---author:jeecg ---date:20260512  for：【QQYUN-15337】online表单支持多数据源，数据源删除后同步移除已注册的动态数据源-----------
        removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 导入数据源，逐行保存并汇总错误。
     */
    @Override
    public Result<?> importDataSources(List<SysDataSource> dataSources) throws IOException {
        List<String> errorMessages = new ArrayList<>();
        int successLines = 0;
        for (int i = 0; i < dataSources.size(); i++) {
            SysDataSource dataSource = dataSources.get(i);
            try {
                if (checkDbCode(dataSource.getCode()) > 0) {
                    errorMessages.add("第" + (i + 1) + "行：数据源编码[" + dataSource.getCode() + "]已存在，忽略导入。");
                    continue;
                }
                if (!save(dataSource)) {
                    throw new JeecgBootException("数据保存失败");
                }
                successLines++;
            } catch (Exception e) {
                errorMessages.add("第" + (i + 1) + "行：" + e.getMessage());
                log.error("导入数据源失败，行号：{}", i + 1, e);
            }
        }
        return ImportExcelUtil.imporReturnRes(errorMessages.size(), successLines, errorMessages);
    }

    /**
     * 动态添加数据源 【注册mybatis动态数据源】
     *
     * @param sysDataSource 添加数据源数据对象
     * @param dbPassword    未加密的密码
     */
    private void addDynamicDataSource(SysDataSource sysDataSource, String dbPassword) {
        DataSourceProperty dataSourceProperty = new DataSourceProperty();
        dataSourceProperty.setUrl(sysDataSource.getDbUrl());
        dataSourceProperty.setPassword(dbPassword);
        dataSourceProperty.setDriverClassName(sysDataSource.getDbDriver());
        dataSourceProperty.setUsername(sysDataSource.getDbUsername());
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
        try {
            ds.addDataSource(sysDataSource.getCode(), dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除数据源
     * @param code
     */
    private void removeDynamicDataSource(String code) {
        //update-begin---author:jeecg ---date:20260512  for：【QQYUN-15337】online表单支持多数据源，移除前先判断是否已注册，避免移除不存在的数据源时抛异常-----------
        try {
            if (!(dataSource instanceof DynamicRoutingDataSource)) {
                return;
            }
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            if (ds.getDataSources() != null && ds.getDataSources().containsKey(code)) {
                ds.removeDataSource(code);
            }
        } catch (Exception e) {
            log.warn("移除动态数据源[{}]失败：{}", code, e.getMessage());
        }
        //update-end---author:jeecg ---date:20260512  for：【QQYUN-15337】online表单支持多数据源，移除前先判断是否已注册，避免移除不存在的数据源时抛异常-----------
    }

    /**
     * 检查数据源编码是否存在
     *
     * @param dbCode
     * @return
     */
    private long checkDbCode(String dbCode) {
        QueryWrapper<SysDataSource> qw = new QueryWrapper();
        qw.lambda().eq(true, SysDataSource::getCode, dbCode);
        return count(qw);
    }

    //update-begin---author:jeecg ---date:20260513  for：【QQYUN-15337】online表单多数据源：唯一性校验等场景需要按 code 把 sys_data_source 数据源懒注册到 baomidou 动态数据源池-----------
    @Override
    public void ensureRegistered(String code) {
        if (StringUtils.isBlank(code)) {
            return;
        }
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            return;
        }
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        // 已注册过 → 直接复用
        if (ds.getDataSources() != null && ds.getDataSources().containsKey(code)) {
            return;
        }
        try {
            SysDataSource d = baseMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysDataSource>()
                            .eq(SysDataSource::getCode, code));
            if (d == null) {
                log.warn("ensureRegistered 失败：sys_data_source 不存在 code=[{}]", code);
                return;
            }
            // 解密密码后注册
            String dbPassword = d.getDbPassword();
            if (StringUtils.isNotBlank(dbPassword)) {
                dbPassword = SecurityUtil.jiemi(dbPassword);
            }
            addDynamicDataSource(d, dbPassword);
        } catch (Exception e) {
            log.warn("ensureRegistered 注册动态数据源[{}]失败：{}", code, e.getMessage());
        }
    }
    //update-end---author:jeecg ---date:20260513  for：【QQYUN-15337】online表单多数据源：唯一性校验等场景需要按 code 把 sys_data_source 数据源懒注册到 baomidou 动态数据源池-----------

}
