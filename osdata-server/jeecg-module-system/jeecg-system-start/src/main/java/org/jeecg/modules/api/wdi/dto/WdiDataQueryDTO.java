package org.jeecg.modules.api.wdi.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WDI面板数据查询入参
 * 指标最多10个、国家最多10个(超限返回提示引导导出), 大表性能防护见WdiDataQueryServiceImpl
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "WDI面板数据查询入参")
public class WdiDataQueryDTO {

    /**指标编码列表(必选, 最多10个, 如 SP.POP.TOTL)*/
    @Schema(description = "指标编码列表(必选, 最多10个, 如 SP.POP.TOTL)", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> seriesCodeList;

    /**国家编码列表(必选, 最多10个, ISO3如 CHN/USA)*/
    @Schema(description = "国家编码列表(必选, 最多10个, ISO3如 CHN/USA)", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> countryCodeList;

    /**起始年份(可选, 含边界)*/
    @Schema(description = "起始年份(可选, 含边界)")
    private Integer startYear;

    /**结束年份(可选, 含边界)*/
    @Schema(description = "结束年份(可选, 含边界)")
    private Integer endYear;

    /**是否返回脚注(默认false, true时关联t_wdi_footnote带出脚注内容)*/
    @Schema(description = "是否返回脚注(默认false, true时关联t_wdi_footnote带出脚注内容)")
    private Boolean includeFootnote = Boolean.TRUE;

    /**返回视图: TABLE_LONG长表(默认)/TABLE_WIDE宽表透视/TIME_SERIES指标时序(每国家一行,指标×年份二级表头)/CHART图表数据*/
    @Schema(description = "返回视图: TABLE_LONG长表(默认)/TABLE_WIDE宽表透视/TIME_SERIES指标时序(每国家一行,指标×年份二级表头)/CHART图表数据")
    private String viewType;
}
