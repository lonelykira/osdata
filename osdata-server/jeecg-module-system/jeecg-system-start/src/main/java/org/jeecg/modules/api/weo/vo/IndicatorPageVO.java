package org.jeecg.modules.api.weo.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO指标元信息VO(指标分页检索/批量获取指标共用)
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO指标元信息")
public class IndicatorPageVO {

    /**指标ID(t_weo_indicator主键, 序列化为字符串避免JS Long精度丢失)*/
    @Schema(description = "指标ID(t_weo_indicator主键)")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**指标代码(如 NGDP/PCPI)*/
    @Schema(description = "指标代码(如 NGDP/PCPI)")
    private String indicatorCode;

    /**指标名称(英文)*/
    @Schema(description = "指标名称(英文)")
    private String indicatorName;

    /**指标名称(中文)*/
    @Schema(description = "指标名称(中文)")
    private String indicatorNameCn;

    /**主题编码(即主题英文名)*/
    @Schema(description = "主题编码(即主题英文名)")
    private String topicCode;

    /**主题名称(中文)*/
    @Schema(description = "主题名称(中文)")
    private String topicCn;

    /**单位*/
    @Schema(description = "单位")
    private String unit;

    /**量级(Billions/Units等)*/
    @Schema(description = "量级(Billions/Units等)")
    private String scale;
}
