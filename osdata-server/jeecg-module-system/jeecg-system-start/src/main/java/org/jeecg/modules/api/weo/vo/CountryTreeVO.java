package org.jeecg.modules.api.weo.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO国家/经济体分组树VO(一级节点为类型分组: 单个国家/聚合经济体, 子节点为成员列表)
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO国家/经济体分组树节点")
public class CountryTreeVO {

    /**分组编码: COUNTRY=单个国家, AGGREGATE=聚合经济体/区域分组*/
    @Schema(description = "分组编码: COUNTRY=单个国家, AGGREGATE=聚合经济体/区域分组")
    private String groupCode;

    /**分组名称(英文)*/
    @Schema(description = "分组名称(英文)")
    private String groupName;

    /**分组名称(中文)*/
    @Schema(description = "分组名称(中文)")
    private String groupNameCn;

    /**子节点: 该分组下的国家/经济体列表*/
    @Schema(description = "子节点: 该分组下的国家/经济体列表")
    private List<CountryNode> children;

    /**
     * 国家/经济体叶子节点(前端多选组件使用)
     */
    @Data
    @Schema(description = "分组下的国家/经济体节点")
    public static class CountryNode {

        /**国家/经济体ID(t_weo_country主键)*/
        @Schema(description = "国家/经济体ID(t_weo_country主键)")
        private Long id;

        /**国家/经济体代码(ISO3如 CHN, 或聚合经济体编码)*/
        @Schema(description = "国家/经济体代码(ISO3如 CHN, 或聚合经济体编码)")
        private String countryCode;

        /**名称(英文)*/
        @Schema(description = "名称(英文)")
        private String countryEn;

        /**名称(中文)*/
        @Schema(description = "名称(中文)")
        private String countryCn;

        /**是否聚合经济体: 0=单个国家 1=聚合经济体/区域分组*/
        @Schema(description = "是否聚合经济体: 0=单个国家 1=聚合经济体/区域分组")
        private Integer isAggregate;
    }
}
