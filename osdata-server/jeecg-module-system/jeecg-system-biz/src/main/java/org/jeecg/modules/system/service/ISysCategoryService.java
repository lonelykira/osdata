package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.model.TreeSelectModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description: 分类字典
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
public interface ISysCategoryService extends IService<SysCategory> {

	/**根节点父ID的值*/
	public static final String ROOT_PID_VALUE = "0";

    /**
     * 存在子节点
     */
    public static final String HAS_CHILD = "1";

    /**
     * 添加分类字典
     * @param sysCategory
     */
	JSONObject addSysCategory(SysCategory sysCategory);

    /**
     * 修改分类字典
     * @param sysCategory
     */
	void updateSysCategory(SysCategory sysCategory);
	
	/**
     * 根据父级编码加载分类字典的数据
	 * @param pcode
	 * @return
     * @throws JeecgBootException
	 */
	public List<TreeSelectModel> queryListByCode(String pcode) throws JeecgBootException;
	
	/**
	  * 根据pid查询子节点集合
	 * @param pid
	 * @return
	 */
	public List<TreeSelectModel> queryListByPid(String pid);

	/**
	 * 根据pid查询子节点集合,支持查询条件
	 * @param pid
	 * @param condition
	 * @return
	 */
	public List<TreeSelectModel> queryListByPid(String pid, Map<String,String> condition);

	/**
	 * 根据code查询id
	 * @param code
	 * @return
	 */
	public String queryIdByCode(String code);

	/**
	 * 删除节点时同时删除子节点及修改父级节点
	 * @param ids
	 */
	void deleteSysCategory(String ids);

	/**
	 * 从分类列表中筛选选中节点及其全部子节点
	 *
	 * @param categoryList 分类列表
	 * @param selectedIds 选中节点ID
	 * @return 选中节点及其全部子节点
	 */
	List<SysCategory> filterSelectionWithChildren(List<SysCategory> categoryList, Collection<String> selectedIds);

	/**
	 * 按父子层级排列分类列表
	 *
	 * @param categoryList 分类列表
	 * @return 父节点在前、子节点紧随其后的分类列表
	 */
	List<SysCategory> sortByHierarchy(List<SysCategory> categoryList);

	/**
	 * 分类字典控件数据回显[表单页面]
	 *
	 * @param ids
	 * @return
	 */
	List<String> loadDictItem(String ids);

	/**
	 * 分类字典控件数据回显[表单页面]
	 *
	 * @param ids
	 * @param delNotExist 是否移除不存在的项，设为false如果某个key不存在数据库中，则直接返回key本身
	 * @return
	 */
	List<String> loadDictItem(String ids, boolean delNotExist);

	/**
	 * 【仅导入使用】分类字典控件反向翻译
	 *
	 * @param names
	 * @param delNotExist 是否移除不存在的项，设为false如果某个key不存在数据库中，则直接返回key本身
	 * @return
	 */
	List<String> loadDictItemByNames(String names, boolean delNotExist);

}
