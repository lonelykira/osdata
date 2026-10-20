package org.jeecg.modules.weo.controller;

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
import org.jeecg.modules.weo.entity.WeoCountry;
import org.jeecg.modules.weo.service.IWeoCountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
 /**
 * @Description: 世界经济组织国家
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Tag(name="世界经济组织国家")
@RestController
@RequestMapping("/weo/weoCountry")
@Slf4j
public class WeoCountryController {
	@Autowired
	private IWeoCountryService weoCountryService;
	
	/**
	 * 分页列表查询
	 *
	 * @param weoCountry
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "世界经济组织国家-分页列表查询")
	@Operation(summary="世界经济组织国家-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<WeoCountry>> queryPageList(WeoCountry weoCountry,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<WeoCountry> queryWrapper = QueryGenerator.initQueryWrapper(weoCountry, req.getParameterMap());
		queryWrapper.orderByAsc("sort_order");
		Page<WeoCountry> page = new Page<WeoCountry>(pageNo, pageSize);
		IPage<WeoCountry> pageList = weoCountryService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param weoCountry
	 * @return
	 */
	@AutoLog(value = "世界经济组织国家-添加")
	@Operation(summary="世界经济组织国家-添加")
	@RequiresPermissions("weo:t_weo_country:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody WeoCountry weoCountry) {
		weoCountryService.save(weoCountry);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param weoCountry
	 * @return
	 */
	@AutoLog(value = "世界经济组织国家-编辑")
	@Operation(summary="世界经济组织国家-编辑")
	@RequiresPermissions("weo:t_weo_country:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody WeoCountry weoCountry) {
		weoCountryService.updateById(weoCountry);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "世界经济组织国家-通过id删除")
	@Operation(summary="世界经济组织国家-通过id删除")
	@RequiresPermissions("weo:t_weo_country:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		weoCountryService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "世界经济组织国家-批量删除")
	@Operation(summary="世界经济组织国家-批量删除")
	@RequiresPermissions("weo:t_weo_country:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.weoCountryService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "世界经济组织国家-通过id查询")
	@Operation(summary="世界经济组织国家-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<WeoCountry> queryById(@RequestParam(name="id",required=true) String id) {
		WeoCountry weoCountry = weoCountryService.getById(id);
		if(weoCountry==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(weoCountry);
	}

}
