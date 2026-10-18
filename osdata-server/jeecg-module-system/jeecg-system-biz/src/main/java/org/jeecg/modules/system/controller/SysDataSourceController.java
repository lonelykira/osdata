package org.jeecg.modules.system.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.util.security.JdbcSecurityUtil;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.config.sign.annotation.SignatureCheck;
import org.jeecg.modules.system.entity.SysDataSource;
import org.jeecg.modules.system.service.ISysDataSourceService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 多数据源管理
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "多数据源管理")
@RestController
@RequestMapping("/sys/dataSource")
public class SysDataSourceController extends JeecgController<SysDataSource, ISysDataSourceService> {

    @Autowired
    private ISysDataSourceService sysDataSourceService;


    /**
     * 分页列表查询
     *
     * @param sysDataSource
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "多数据源管理-分页列表查询")
    @Operation(summary = "多数据源管理-分页列表查询")
    @RequiresPermissions("system:datasource:list")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(
            SysDataSource sysDataSource,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        //------------------------------------------------------------------------------------------------
        //是否开启系统管理模块的多租户数据隔离【SAAS多租户模式】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            sysDataSource.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        //------------------------------------------------------------------------------------------------
        QueryWrapper<SysDataSource> queryWrapper = QueryGenerator.initQueryWrapper(sysDataSource, req.getParameterMap());
        Page<SysDataSource> page = new Page<>(pageNo, pageSize);
        IPage<SysDataSource> pageList = sysDataSourceService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 下拉选项数据 (online报表使用)
     * @param sysDataSource
     * @param req
     * @return
     */
    @SignatureCheck
    @RequiresPermissions("online:report:add")
    @GetMapping(value = "/options")
    public Result<?> queryOptions(SysDataSource sysDataSource, HttpServletRequest req) {
        //------------------------------------------------------------------------------------------------
        //是否开启系统管理模块的多租户数据隔离【SAAS多租户模式】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            sysDataSource.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        //------------------------------------------------------------------------------------------------
        
        QueryWrapper<SysDataSource> queryWrapper = QueryGenerator.initQueryWrapper(sysDataSource, req.getParameterMap());
        List<SysDataSource> pageList = sysDataSourceService.list(queryWrapper);
        JSONArray array = new JSONArray(pageList.size());
        for (SysDataSource item : pageList) {
            JSONObject option = new JSONObject(3);
            option.put("value", item.getCode());
            option.put("label", item.getName());
            option.put("text", item.getName());
            array.add(option);
        }
        return Result.ok(array);
    }

    /**
     * 添加
     *
     * @param sysDataSource
     * @return
     */
    @AutoLog(value = "多数据源管理-添加")
    @Operation(summary = "多数据源管理-添加")
    @RequiresPermissions("system:datasource:add")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysDataSource sysDataSource) {
        // 代码逻辑说明: jdbc连接地址漏洞问题
        try {
            JdbcSecurityUtil.validate(sysDataSource.getDbUrl());
            JdbcSecurityUtil.validateDriver(sysDataSource.getDbDriver());
        }catch (JeecgBootException e){
            log.error(e.toString());
            return Result.error("操作失败：" + e.getMessage());
        }
        return sysDataSourceService.saveDataSource(sysDataSource);
    }

    /**
     * 编辑
     *
     * @param sysDataSource
     * @return
     */
    @AutoLog(value = "多数据源管理-编辑")
    @Operation(summary = "多数据源管理-编辑")
    @RequiresPermissions("system:datasource:edit")
    @RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody SysDataSource sysDataSource) {
        // 代码逻辑说明: jdbc连接地址漏洞问题
        try {
            JdbcSecurityUtil.validate(sysDataSource.getDbUrl());
            JdbcSecurityUtil.validateDriver(sysDataSource.getDbDriver());
        } catch (JeecgBootException e) {
            log.error(e.toString());
            return Result.error("操作失败：" + e.getMessage());
        }
        return sysDataSourceService.editDataSource(sysDataSource);
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "多数据源管理-通过id删除")
    @Operation(summary = "多数据源管理-通过id删除")
    @RequiresPermissions("system:datasource:delete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return sysDataSourceService.deleteDataSource(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "多数据源管理-批量删除")
    @Operation(summary = "多数据源管理-批量删除")
    @RequiresPermissions("system:datasource:delete")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        idList.forEach(item->{
            SysDataSource sysDataSource = sysDataSourceService.getById(item);
            DataSourceCachePool.removeCache(sysDataSource.getCode());
        });
        this.sysDataSourceService.removeByIds(idList);
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "多数据源管理-通过id查询")
    @Operation(summary = "多数据源管理-通过id查询")
    @RequiresPermissions("system:datasource:list")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        SysDataSource sysDataSource = sysDataSourceService.getById(id);
        // 出于安全考虑，不向前端返回数据库密码（密码字段已通过 @JsonProperty(WRITE_ONLY) 屏蔽序列化，编辑时密码留空表示不修改）
        return Result.ok(sysDataSource);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param sysDataSource
     */
    @RequiresPermissions("system:datasource:export")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysDataSource sysDataSource) {
        //------------------------------------------------------------------------------------------------
        //是否开启系统管理模块的多租户数据隔离【SAAS多租户模式】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            sysDataSource.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        //------------------------------------------------------------------------------------------------
        return super.exportXls(request, sysDataSource, SysDataSource.class, "多数据源管理");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("system:datasource:import")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SysDataSource> dataSources = ExcelImportUtil.importExcel(file.getInputStream(), SysDataSource.class, params);
                return sysDataSourceService.importDataSources(dataSources);
            } catch (Exception e) {
                log.error("文件导入失败", e);
                return Result.error("文件导入失败:" + e.getMessage());
            }
        }
        return Result.error("文件导入失败！");
    }



}
