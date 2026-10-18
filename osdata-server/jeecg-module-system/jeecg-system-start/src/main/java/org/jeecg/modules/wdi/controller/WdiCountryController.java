package org.jeecg.modules.wdi.controller;

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
import org.jeecg.modules.wdi.entity.WdiCountry;
import org.jeecg.modules.wdi.service.IWdiCountryService;

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
 * @Description: 世行WDI国家/经济体信息表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Tag(name="世行WDI国家/经济体信息表")
@RestController
@RequestMapping("/wdi/wdiCountry")
@Slf4j
public class WdiCountryController extends JeecgController<WdiCountry, IWdiCountryService> {
	@Autowired
	private IWdiCountryService wdiCountryService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wdiCountry
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "世行WDI国家/经济体信息表-分页列表查询")
	@Operation(summary="世行WDI国家/经济体信息表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<WdiCountry>> queryPageList(WdiCountry wdiCountry,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<WdiCountry> queryWrapper = QueryGenerator.initQueryWrapper(wdiCountry, req.getParameterMap());
		Page<WdiCountry> page = new Page<WdiCountry>(pageNo, pageSize);
		IPage<WdiCountry> pageList = wdiCountryService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wdiCountry
	 * @return
	 */
	@AutoLog(value = "世行WDI国家/经济体信息表-添加")
	@Operation(summary="世行WDI国家/经济体信息表-添加")
	@RequiresPermissions("wdi:t_wdi_country:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody WdiCountry wdiCountry) {
		wdiCountryService.save(wdiCountry);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wdiCountry
	 * @return
	 */
	@AutoLog(value = "世行WDI国家/经济体信息表-编辑")
	@Operation(summary="世行WDI国家/经济体信息表-编辑")
	@RequiresPermissions("wdi:t_wdi_country:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody WdiCountry wdiCountry) {
		wdiCountryService.updateById(wdiCountry);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "世行WDI国家/经济体信息表-通过id删除")
	@Operation(summary="世行WDI国家/经济体信息表-通过id删除")
	@RequiresPermissions("wdi:t_wdi_country:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		wdiCountryService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "世行WDI国家/经济体信息表-批量删除")
	@Operation(summary="世行WDI国家/经济体信息表-批量删除")
	@RequiresPermissions("wdi:t_wdi_country:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wdiCountryService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "世行WDI国家/经济体信息表-通过id查询")
	@Operation(summary="世行WDI国家/经济体信息表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<WdiCountry> queryById(@RequestParam(name="id",required=true) String id) {
		WdiCountry wdiCountry = wdiCountryService.getById(id);
		if(wdiCountry==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(wdiCountry);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wdiCountry
    */
    @RequiresPermissions("wdi:t_wdi_country:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WdiCountry wdiCountry) {
        return super.exportXls(request, wdiCountry, WdiCountry.class, "世行WDI国家/经济体信息表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("wdi:t_wdi_country:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, WdiCountry.class);
    }

}
