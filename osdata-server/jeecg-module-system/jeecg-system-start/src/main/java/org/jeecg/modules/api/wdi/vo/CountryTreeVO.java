package org.jeecg.modules.api.wdi.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 国家分组树VO(一级节点为区域region, 子节点为该区域下的国家列表, 附带收入分组标签)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "国家分组树节点")
public class CountryTreeVO {

    /**区域名称(英文)*/
    @Schema(description = "区域名称(英文)")
    private String region;

    /**区域名称(中文)*/
    @Schema(description = "区域名称(中文)")
    private String regionCn;

    /**子节点: 该区域下的国家列表*/
    @Schema(description = "子节点: 该区域下的国家列表")
    private List<CountryNode> children;

    /**
     * 国家叶子节点(附带收入分组标签, 前端多选组件使用)
     */
    @Data
    @Schema(description = "区域下的国家节点")
    public static class CountryNode {

        /**国家ID(t_wdi_country主键)*/
        @Schema(description = "国家ID(t_wdi_country主键)")
        private Long id;

        /**国家/经济体代码(ISO3, 如 CHN)*/
        @Schema(description = "国家/经济体代码(ISO3, 如 CHN)")
        private String countryCode;

        /**国家简称(英文)*/
        @Schema(description = "国家简称(英文)")
        private String shortName;

        /**国家中文名*/
        @Schema(description = "国家中文名")
        private String countryNameCn;

        /**收入分组(英文, 如 Upper middle income)*/
        @Schema(description = "收入分组(英文, 如 Upper middle income)")
        private String incomeGroup;

        /**收入分组(中文)*/
        @Schema(description = "收入分组(中文)")
        private String incomeGroupCn;
    }
}
