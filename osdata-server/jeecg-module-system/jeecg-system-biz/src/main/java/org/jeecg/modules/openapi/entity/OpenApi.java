package org.jeecg.modules.openapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApi  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 请求方式，如POST、GET
     */
    private String requestMethod;

    /**
     * 对外开放的相对接口路径
     */
    private String requestUrl;

    /**
     * IP 白名单
     */
    private String whiteList;

    //update-begin---author:scott ---date:20260417  for：【PR/9083】OpenAPI新增白名单备注字段-----------
    /**
     * 白名单备注说明
     */
    private String remarks;
    //update-end---author:scott ---date:20260417  for：【PR/9083】OpenAPI新增白名单备注字段-----------
    //update-begin---author:liusq ---date:20260805  for：LHZP-1356 接口描述-----------
    /**
     * 接口描述
     */
    private String description;
    //update-end---author:liusq ---date:20260805  for：LHZP-1356 接口描述-----------
    /**
     * 请求头json
     */
    private String headersJson;
    /**
     * 请求参数json
     */
    private String paramsJson;


    /**
     * 目前仅支持json
     */
    private String requestBody;

    /**
     * 原始接口路径
     */
    private String originUrl;

    /**
     * 状态(1：正常  2：废弃 ）
     */
    private Integer status;

    /**
     * 删除状态（0，正常，1已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 历史已选接口
     */
    @TableField(exist = false)
    private String ifCheckBox = "0";
}
