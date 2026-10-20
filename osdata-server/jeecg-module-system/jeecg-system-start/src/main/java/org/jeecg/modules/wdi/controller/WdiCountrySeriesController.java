package org.jeecg.modules.wdi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.wdi.entity.WdiCountrySeries;
import org.jeecg.modules.wdi.service.IWdiCountrySeriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
 /**
 * @Description: 世行WDI国家-指标覆盖表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Tag(name="世行WDI国家-指标覆盖表")
@RestController
@RequestMapping("/wdi/wdiCountrySeries")
@Slf4j
public class WdiCountrySeriesController  {
	@Autowired
	private IWdiCountrySeriesService wdiCountrySeriesService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wdiCountrySeries
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "世行WDI国家-指标覆盖表-分页列表查询")
	@Operation(summary="世行WDI国家-指标覆盖表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<WdiCountrySeries>> queryPageList(WdiCountrySeries wdiCountrySeries,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<WdiCountrySeries> queryWrapper = QueryGenerator.initQueryWrapper(wdiCountrySeries, req.getParameterMap());
		Page<WdiCountrySeries> page = new Page<WdiCountrySeries>(pageNo, pageSize);
		IPage<WdiCountrySeries> pageList = wdiCountrySeriesService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wdiCountrySeries
	 * @return
	 */
	@AutoLog(value = "世行WDI国家-指标覆盖表-添加")
	@Operation(summary="世行WDI国家-指标覆盖表-添加")
	@RequiresPermissions("wdi:t_wdi_country_series:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody WdiCountrySeries wdiCountrySeries) {
		wdiCountrySeriesService.save(wdiCountrySeries);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wdiCountrySeries
	 * @return
	 */
	@AutoLog(value = "世行WDI国家-指标覆盖表-编辑")
	@Operation(summary="世行WDI国家-指标覆盖表-编辑")
	@RequiresPermissions("wdi:t_wdi_country_series:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody WdiCountrySeries wdiCountrySeries) {
		wdiCountrySeriesService.updateById(wdiCountrySeries);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "世行WDI国家-指标覆盖表-通过id删除")
	@Operation(summary="世行WDI国家-指标覆盖表-通过id删除")
	@RequiresPermissions("wdi:t_wdi_country_series:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		wdiCountrySeriesService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "世行WDI国家-指标覆盖表-批量删除")
	@Operation(summary="世行WDI国家-指标覆盖表-批量删除")
	@RequiresPermissions("wdi:t_wdi_country_series:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wdiCountrySeriesService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "世行WDI国家-指标覆盖表-通过id查询")
	@Operation(summary="世行WDI国家-指标覆盖表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<WdiCountrySeries> queryById(@RequestParam(name="id",required=true) String id) {
		WdiCountrySeries wdiCountrySeries = wdiCountrySeriesService.getById(id);
		if(wdiCountrySeries==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(wdiCountrySeries);
	}

}
