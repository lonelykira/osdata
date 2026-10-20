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
import org.jeecg.modules.wdi.entity.WdiData;
import org.jeecg.modules.wdi.service.IWdiDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
 /**
 * @Description: 世行WDI面板数据长表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Tag(name="世行WDI面板数据长表")
@RestController
@RequestMapping("/wdi/wdiData")
@Slf4j
public class WdiDataController {
	@Autowired
	private IWdiDataService wdiDataService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wdiData
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "世行WDI面板数据长表-分页列表查询")
	@Operation(summary="世行WDI面板数据长表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<WdiData>> queryPageList(WdiData wdiData,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<WdiData> queryWrapper = QueryGenerator.initQueryWrapper(wdiData, req.getParameterMap());
		Page<WdiData> page = new Page<WdiData>(pageNo, pageSize);
		IPage<WdiData> pageList = wdiDataService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wdiData
	 * @return
	 */
	@AutoLog(value = "世行WDI面板数据长表-添加")
	@Operation(summary="世行WDI面板数据长表-添加")
	@RequiresPermissions("wdi:t_wdi_data:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody WdiData wdiData) {
		wdiDataService.save(wdiData);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wdiData
	 * @return
	 */
	@AutoLog(value = "世行WDI面板数据长表-编辑")
	@Operation(summary="世行WDI面板数据长表-编辑")
	@RequiresPermissions("wdi:t_wdi_data:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody WdiData wdiData) {
		wdiDataService.updateById(wdiData);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "世行WDI面板数据长表-通过id删除")
	@Operation(summary="世行WDI面板数据长表-通过id删除")
	@RequiresPermissions("wdi:t_wdi_data:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		wdiDataService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "世行WDI面板数据长表-批量删除")
	@Operation(summary="世行WDI面板数据长表-批量删除")
	@RequiresPermissions("wdi:t_wdi_data:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wdiDataService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "世行WDI面板数据长表-通过id查询")
	@Operation(summary="世行WDI面板数据长表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<WdiData> queryById(@RequestParam(name="id",required=true) String id) {
		WdiData wdiData = wdiDataService.getById(id);
		if(wdiData==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(wdiData);
	}

    /**
      * 通过excel导入数据
    *
    * @param file
    * @return
    */
	@AutoLog(value = "世行WDI面板数据长表数据导入")
    @RequiresPermissions("wdi:t_wdi_data:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(@RequestParam(name = "file") MultipartFile file) {
		wdiDataService.importExcel(file);
		return Result.ok("导入成功");
	}

}
