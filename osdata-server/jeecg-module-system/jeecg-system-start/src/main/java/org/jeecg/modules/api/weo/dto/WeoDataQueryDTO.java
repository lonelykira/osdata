package org.jeecg.modules.api.weo.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO面板数据查询入参
 * 指标最多10个、国家最多10个(超限直接抛异常提示减少数量), 大表性能防护见WeoApiServiceImpl
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO面板数据查询入参")
public class WeoDataQueryDTO {

    /**指标编码列表(必选, 最多10个, 如 NGDP/PCPI)*/
    @Schema(description = "指标编码列表(必选, 最多10个, 如 NGDP/PCPI)", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> indicatorCodeList;

    /**国家/经济体编码列表(必选, 最多10个, ISO3如 CHN/USA, 含聚合经济体编码)*/
    @Schema(description = "国家/经济体编码列表(必选, 最多10个, ISO3如 CHN/USA, 含聚合经济体编码)", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> countryCodeList;

    /**起始年份(可选, 含边界, 数据范围1980~2031)*/
    @Schema(description = "起始年份(可选, 含边界, 数据范围1980~2031)")
    private Integer startYear;

    /**结束年份(可选, 含边界, 数据范围1980~2031)*/
    @Schema(description = "结束年份(可选, 含边界, 数据范围1980~2031)")
    private Integer endYear;

    /**返回视图: TABLE_LONG长表(默认)/TABLE_WIDE宽表透视/CHART图表数据*/
    @Schema(description = "返回视图: TABLE_LONG长表(默认)/TABLE_WIDE宽表透视/CHART图表数据")
    private String viewType;
}
