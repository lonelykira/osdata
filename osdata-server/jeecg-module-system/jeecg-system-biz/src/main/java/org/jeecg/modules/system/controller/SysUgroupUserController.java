package org.jeecg.modules.system.controller;

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
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysUgroupUser;
import org.jeecg.modules.system.service.ISysUgroupUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
 /**
 * @Description: 用户组关系表
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Tag(name="用户组关系表")
@RestController
@RequestMapping("/system/sysUgroupUser")
@Slf4j
public class SysUgroupUserController extends JeecgController<SysUgroupUser, ISysUgroupUserService> {
	@Autowired
	private ISysUgroupUserService sysUgroupUserService;
	
	/**
	 * 分页列表查询
	 *
	 * @param sysUgroupUser
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "用户组关系表-分页列表查询")
	@Operation(summary="用户组关系表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<SysUgroupUser>> queryPageList(SysUgroupUser sysUgroupUser,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<SysUgroupUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUgroupUser, req.getParameterMap());
		Page<SysUgroupUser> page = new Page<SysUgroupUser>(pageNo, pageSize);
		IPage<SysUgroupUser> pageList = sysUgroupUserService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param sysUgroupUser
	 * @return
	 */
	@AutoLog(value = "用户组关系表-添加")
	@Operation(summary="用户组关系表-添加")
	@RequiresPermissions("system:sys_ugroup_user:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody SysUgroupUser sysUgroupUser) {
		sysUgroupUserService.save(sysUgroupUser);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param sysUgroupUser
	 * @return
	 */
	@AutoLog(value = "用户组关系表-编辑")
	@Operation(summary="用户组关系表-编辑")
	@RequiresPermissions("system:sys_ugroup_user:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody SysUgroupUser sysUgroupUser) {
		sysUgroupUserService.updateById(sysUgroupUser);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "用户组关系表-通过id删除")
	@Operation(summary="用户组关系表-通过id删除")
	@RequiresPermissions("system:sys_ugroup_user:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		sysUgroupUserService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "用户组关系表-批量删除")
	@Operation(summary="用户组关系表-批量删除")
	@RequiresPermissions("system:sys_ugroup_user:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysUgroupUserService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "用户组关系表-通过id查询")
	@Operation(summary="用户组关系表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<SysUgroupUser> queryById(@RequestParam(name="id",required=true) String id) {
		SysUgroupUser sysUgroupUser = sysUgroupUserService.getById(id);
		if(sysUgroupUser==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(sysUgroupUser);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param sysUgroupUser
    */
    @RequiresPermissions("system:sys_ugroup_user:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysUgroupUser sysUgroupUser) {
        return super.exportXls(request, sysUgroupUser, SysUgroupUser.class, "用户组关系表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("system:sys_ugroup_user:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysUgroupUser.class);
    }

}
