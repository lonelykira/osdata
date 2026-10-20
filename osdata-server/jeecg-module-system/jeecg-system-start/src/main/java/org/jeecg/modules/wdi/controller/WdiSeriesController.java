package org.jeecg.modules.wdi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.wdi.entity.WdiSeries;
import org.jeecg.modules.wdi.service.IWdiSeriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
 /**
 * @Description: 世行WDI指标元数据表(含中文字段)
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Tag(name="世行WDI指标元数据表(含中文字段)")
@RestController
@RequestMapping("/wdi/wdiSeries")
@Slf4j
public class WdiSeriesController {
	@Autowired
	private IWdiSeriesService wdiSeriesService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wdiSeries
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "世行WDI指标元数据表(含中文字段)-分页列表查询")
	@Operation(summary="世行WDI指标元数据表(含中文字段)-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<WdiSeries>> queryPageList(WdiSeries wdiSeries,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<WdiSeries> queryWrapper = QueryGenerator.initQueryWrapper(wdiSeries, req.getParameterMap());
		Page<WdiSeries> page = new Page<WdiSeries>(pageNo, pageSize);
		IPage<WdiSeries> pageList = wdiSeriesService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wdiSeries
	 * @return
	 */
	@AutoLog(value = "世行WDI指标元数据表(含中文字段)-添加")
	@Operation(summary="世行WDI指标元数据表(含中文字段)-添加")
	@RequiresPermissions("wdi:t_wdi_series:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody WdiSeries wdiSeries) {
		wdiSeriesService.save(wdiSeries);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wdiSeries
	 * @return
	 */
	@AutoLog(value = "世行WDI指标元数据表(含中文字段)-编辑")
	@Operation(summary="世行WDI指标元数据表(含中文字段)-编辑")
	@RequiresPermissions("wdi:t_wdi_series:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody WdiSeries wdiSeries) {
		wdiSeriesService.updateById(wdiSeries);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "世行WDI指标元数据表(含中文字段)-通过id删除")
	@Operation(summary="世行WDI指标元数据表(含中文字段)-通过id删除")
	@RequiresPermissions("wdi:t_wdi_series:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		wdiSeriesService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "世行WDI指标元数据表(含中文字段)-批量删除")
	@Operation(summary="世行WDI指标元数据表(含中文字段)-批量删除")
	@RequiresPermissions("wdi:t_wdi_series:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wdiSeriesService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "世行WDI指标元数据表(含中文字段)-通过id查询")
	@Operation(summary="世行WDI指标元数据表(含中文字段)-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<WdiSeries> queryById(@RequestParam(name="id",required=true) String id) {
		WdiSeries wdiSeries = wdiSeriesService.getById(id);
		if(wdiSeries==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(wdiSeries);
	}



}
