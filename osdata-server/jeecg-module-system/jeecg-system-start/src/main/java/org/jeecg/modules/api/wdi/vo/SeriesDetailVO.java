package org.jeecg.modules.api.wdi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * @Description: 指标详情VO(完整定义/单位/来源 + 年份覆盖范围)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "指标详情")
public class SeriesDetailVO {

    /**指标ID(t_wdi_series主键, 序列化为字符串避免JS Long精度丢失)*/
    @Schema(description = "指标ID(t_wdi_series主键)")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**指标代码(如 SP.POP.TOTL)*/
    @Schema(description = "指标代码(如 SP.POP.TOTL)")
    private String seriesCode;

    /**指标名称(英文)*/
    @Schema(description = "指标名称(英文)")
    private String indicatorName;

    /**指标名称(中文)*/
    @Schema(description = "指标名称(中文)")
    private String indicatorNameCn;

    /**主题(英文)*/
    @Schema(description = "主题(英文)")
    private String topic;

    /**主题(中文)*/
    @Schema(description = "主题(中文)")
    private String topicCn;

    /**短定义(英文)*/
    @Schema(description = "短定义(英文)")
    private String shortDefinition;

    /**短定义(中文)*/
    @Schema(description = "短定义(中文)")
    private String shortDefinitionCn;

    /**长定义(英文)*/
    @Schema(description = "长定义(英文)")
    private String longDefinition;

    /**长定义(中文)*/
    @Schema(description = "长定义(中文)")
    private String longDefinitionCn;

    /**计量单位(英文)*/
    @Schema(description = "计量单位(英文)")
    private String unit;

    /**计量单位(中文)*/
    @Schema(description = "计量单位(中文)")
    private String unitCn;

    /**数据频率(年/季/月)*/
    @Schema(description = "数据频率(年/季/月)")
    private String periodicity;

    /**基期*/
    @Schema(description = "基期")
    private String basePeriod;

    /**加总方法*/
    @Schema(description = "加总方法")
    private String aggregationMethod;

    /**数据来源机构(英文)*/
    @Schema(description = "数据来源机构(英文)")
    private String source;

    /**数据来源机构(中文)*/
    @Schema(description = "数据来源机构(中文)")
    private String sourceCn;

    /**相关来源链接*/
    @Schema(description = "相关来源链接")
    private String relatedSourceLinks;

    /**许可类型(如 CC BY 4.0)*/
    @Schema(description = "许可类型(如 CC BY 4.0)")
    private String licenseType;

    /**该指标支持的最小年份(来自t_wdi_series_time)*/
    @Schema(description = "该指标支持的最小年份(来自t_wdi_series_time)")
    private Integer minYear;

    /**该指标支持的最大年份(来自t_wdi_series_time)*/
    @Schema(description = "该指标支持的最大年份(来自t_wdi_series_time)")
    private Integer maxYear;
}
