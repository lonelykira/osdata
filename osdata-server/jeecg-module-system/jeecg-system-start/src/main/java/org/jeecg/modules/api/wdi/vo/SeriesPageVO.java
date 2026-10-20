package org.jeecg.modules.api.wdi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * @Description: 指标元信息VO(指标分页检索/批量获取指标元信息共用)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "指标元信息")
public class SeriesPageVO {

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

    /**计量单位(英文)*/
    @Schema(description = "计量单位(英文)")
    private String unit;

    /**计量单位(中文)*/
    @Schema(description = "计量单位(中文)")
    private String unitCn;

    /**数据频率(年/季/月)*/
    @Schema(description = "数据频率(年/季/月)")
    private String periodicity;

    /**数据来源机构(英文)*/
    @Schema(description = "数据来源机构(英文)")
    private String source;

    /**数据来源机构(中文)*/
    @Schema(description = "数据来源机构(中文)")
    private String sourceCn;
}
