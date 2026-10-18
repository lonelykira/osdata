package org.jeecg.modules.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WDI数据脚注VO(includeFootnote=true时随查询结果返回, 来自t_wdi_footnote)
 * 前端按国家代码+指标代码关联到对应数据行/单元格
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "WDI数据脚注(按国家代码+指标代码关联数据, year为空表示全局脚注)")
public class WdiDataFootnoteVO {

    /**国家代码(ISO3, 如 CHN)*/
    @Schema(description = "国家代码(ISO3, 如 CHN)")
    private String countryCode;

    /**国家名称(中文名优先, 无中文名时返回英文简称)*/
    @Schema(description = "国家名称(中文名优先, 无中文名时返回英文简称)")
    private String countryName;

    /**指标编码(如 SP.POP.TOTL)*/
    @Schema(description = "指标编码(如 SP.POP.TOTL)")
    private String seriesCode;

    /**指标名称(中文名优先, 无中文名时返回英文名)*/
    @Schema(description = "指标名称(中文名优先, 无中文名时返回英文名)")
    private String indicatorName;

    /**年份(可为空, 空=该国家该指标的全局脚注)*/
    @Schema(description = "年份(可为空, 空=该国家该指标的全局脚注)")
    private Integer year;

    /**脚注内容(英文)*/
    @Schema(description = "脚注内容(英文)")
    private String footnoteText;

    /**脚注内容(中文)*/
    @Schema(description = "脚注内容(中文)")
    private String footnoteTextCn;
}
