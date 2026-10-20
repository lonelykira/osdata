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
import org.jeecg.modules.wdi.entity.WdiSeriesTime;
import org.jeecg.modules.wdi.service.IWdiSeriesTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
 /**
 * @Description: 世行WDI指标年份覆盖表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Tag(name="世行WDI指标年份覆盖表")
@RestController
@RequestMapping("/wdi/wdiSeriesTime")
@Slf4j
public class WdiSeriesTimeController {
	@Autowired
	private IWdiSeriesTimeService wdiSeriesTimeService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wdiSeriesTime
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "世行WDI指标年份覆盖表-分页列表查询")
	@Operation(summary="世行WDI指标年份覆盖表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<WdiSeriesTime>> queryPageList(WdiSeriesTime wdiSeriesTime,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<WdiSeriesTime> queryWrapper = QueryGenerator.initQueryWrapper(wdiSeriesTime, req.getParameterMap());
		Page<WdiSeriesTime> page = new Page<WdiSeriesTime>(pageNo, pageSize);
		IPage<WdiSeriesTime> pageList = wdiSeriesTimeService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wdiSeriesTime
	 * @return
	 */
	@AutoLog(value = "世行WDI指标年份覆盖表-添加")
	@Operation(summary="世行WDI指标年份覆盖表-添加")
	@RequiresPermissions("wdi:t_wdi_series_time:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody WdiSeriesTime wdiSeriesTime) {
		wdiSeriesTimeService.save(wdiSeriesTime);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wdiSeriesTime
	 * @return
	 */
	@AutoLog(value = "世行WDI指标年份覆盖表-编辑")
	@Operation(summary="世行WDI指标年份覆盖表-编辑")
	@RequiresPermissions("wdi:t_wdi_series_time:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody WdiSeriesTime wdiSeriesTime) {
		wdiSeriesTimeService.updateById(wdiSeriesTime);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "世行WDI指标年份覆盖表-通过id删除")
	@Operation(summary="世行WDI指标年份覆盖表-通过id删除")
	@RequiresPermissions("wdi:t_wdi_series_time:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		wdiSeriesTimeService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "世行WDI指标年份覆盖表-批量删除")
	@Operation(summary="世行WDI指标年份覆盖表-批量删除")
	@RequiresPermissions("wdi:t_wdi_series_time:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wdiSeriesTimeService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "世行WDI指标年份覆盖表-通过id查询")
	@Operation(summary="世行WDI指标年份覆盖表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<WdiSeriesTime> queryById(@RequestParam(name="id",required=true) String id) {
		WdiSeriesTime wdiSeriesTime = wdiSeriesTimeService.getById(id);
		if(wdiSeriesTime==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(wdiSeriesTime);
	}

}
