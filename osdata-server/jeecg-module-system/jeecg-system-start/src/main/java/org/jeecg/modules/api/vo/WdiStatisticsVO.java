package org.jeecg.modules.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WDI统计概览VO(指标总数/国家总数/数据年份跨度/数据记录总数)
 * 由WdiStatisticsComponent在应用启动时通过CompletableFuture多线程查询并预加载到Redis,
 * 接口调用优先读缓存, 避免对约900w行的t_wdi_data大表实时COUNT
 * @Author: jeecg-boot
 * @Date:   2026-10-16
 * @Version: V1.0
 */
@Data
@Schema(description = "WDI统计概览")
public class WdiStatisticsVO {

    /**指标总数(t_wdi_series行数)*/
    @Schema(description = "指标总数", example = "1470")
    private Long seriesCount;

    /**国家总数(t_wdi_country行数)*/
    @Schema(description = "国家总数", example = "266")
    private Long countryCount;

    /**数据最小年份(全部指标年份覆盖t_wdi_series_time的全局最小年份)*/
    @Schema(description = "数据最小年份", example = "1960")
    private Integer minYear;

    /**数据最大年份(全部指标年份覆盖t_wdi_series_time的全局最大年份)*/
    @Schema(description = "数据最大年份", example = "2025")
    private Integer maxYear;

    /**数据记录总数(t_wdi_data行数, 约900w行, 走启动预加载/Redis缓存获取)*/
    @Schema(description = "数据记录总数", example = "9000000")
    private Long dataCount;
}
