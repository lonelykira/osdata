package org.jeecg.modules.weo.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.weo.entity.WeoData;
import org.jeecg.modules.weo.service.IWeoDataService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
 /**
 * @Description: 世界经济组织数据表
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Tag(name="世界经济组织数据表")
@RestController
@RequestMapping("/weo/weoData")
@Slf4j
public class WeoDataController {
	@Autowired
	private IWeoDataService weoDataService;
	
	/**
	 * 分页列表查询
	 *
	 * @param weoData
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "世界经济组织数据表-分页列表查询")
	@Operation(summary="世界经济组织数据表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<WeoData>> queryPageList(WeoData weoData,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<WeoData> queryWrapper = QueryGenerator.initQueryWrapper(weoData, req.getParameterMap());
		Page<WeoData> page = new Page<WeoData>(pageNo, pageSize);
		IPage<WeoData> pageList = weoDataService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param weoData
	 * @return
	 */
	@AutoLog(value = "世界经济组织数据表-添加")
	@Operation(summary="世界经济组织数据表-添加")
	@RequiresPermissions("weo:t_weo_data:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody WeoData weoData) {
		weoDataService.save(weoData);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param weoData
	 * @return
	 */
	@AutoLog(value = "世界经济组织数据表-编辑")
	@Operation(summary="世界经济组织数据表-编辑")
	@RequiresPermissions("weo:t_weo_data:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody WeoData weoData) {
		weoDataService.updateById(weoData);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "世界经济组织数据表-通过id删除")
	@Operation(summary="世界经济组织数据表-通过id删除")
	@RequiresPermissions("weo:t_weo_data:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		weoDataService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "世界经济组织数据表-批量删除")
	@Operation(summary="世界经济组织数据表-批量删除")
	@RequiresPermissions("weo:t_weo_data:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.weoDataService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "世界经济组织数据表-通过id查询")
	@Operation(summary="世界经济组织数据表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<WeoData> queryById(@RequestParam(name="id",required=true) String id) {
		WeoData weoData = weoDataService.getById(id);
		if(weoData==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(weoData);
	}

    /**
      * 通过excel导入数据
    *
    * @param file
    * @return
    */
	@AutoLog(value = "世界经济组织数据表数据导入")
    @RequiresPermissions("weo:t_weo_data:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(@RequestParam(name = "file") MultipartFile file) {
		weoDataService.importExcel(file);
		return Result.ok("导入成功");
	}

}
