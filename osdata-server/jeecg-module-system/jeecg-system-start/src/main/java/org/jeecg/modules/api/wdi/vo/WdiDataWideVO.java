package org.jeecg.modules.api.wdi.vo;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WDI面板数据宽表VO(viewType=TABLE_WIDE)
 * 行=国家×指标组合(单指标时即国家行), 列=年份, 单元格=指标值
 * 由service在Java内存中行转列透视生成(禁止动态SQL行转列)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "WDI面板数据宽表(行=国家×指标, 列=年份, Java内存透视)")
public class WdiDataWideVO {

    /**年份列(升序, 与行values按索引对齐)*/
    @Schema(description = "年份列(升序, 与行values按索引对齐)")
    private List<Integer> yearColumns;

    /**宽表行(国家×指标组合, 缺失年份数值为null)*/
    @Schema(description = "宽表行(国家×指标组合, 缺失年份数值为null)")
    private List<WideRow> rows;

    /**
     * 宽表行: 一行代表一个国家×指标组合
     */
    @Data
    @Schema(description = "宽表行: 一行代表一个国家×指标组合")
    public static class WideRow {

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

        /**单元格值列表(与yearColumns按索引对齐, 缺失年份为null)*/
        @Schema(description = "单元格值列表(与yearColumns按索引对齐, 缺失年份为null)")
        private List<BigDecimal> values;
    }
}
