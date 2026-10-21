package org.jeecg.modules.api.weo.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO面板数据指标时序VO(viewType=TIME_SERIES)
 * 行=国家(每个国家仅一行), 一级表头=指标, 二级表头=年份, 单元格=该国家该指标该年份的数值
 * 由service在Java内存中透视生成(禁止动态SQL行转列)
 * @Author: jeecg-boot
 * @Date:   2026-10-21
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO面板数据指标时序(行=国家, 一级表头=指标, 二级表头=年份, Java内存透视)")
public class WeoDataTimeSeriesVO {

    /**年份子表头(升序, 各指标组的二级表头相同, 与行values按索引对齐)*/
    @Schema(description = "年份子表头(升序, 各指标组的二级表头相同, 与行values按索引对齐)")
    private List<Integer> yearColumns;

    /**指标一级表头(按用户查询顺序)*/
    @Schema(description = "指标一级表头(按用户查询顺序)")
    private List<IndicatorColumn> indicators;

    /**时序行(每个国家仅一行, 仅包含有数据的国家, 缺失指标/年份单元为null)*/
    @Schema(description = "时序行(每个国家仅一行, 仅包含有数据的国家, 缺失指标/年份单元为null)")
    private List<TimeSeriesRow> rows;

    /**
     * 指标列: 一级表头分组
     */
    @Data
    @Schema(description = "指标列: 一级表头分组")
    public static class IndicatorColumn {

        /**指标编码(如 NGDP/PCPI)*/
        @Schema(description = "指标编码(如 NGDP/PCPI)")
        private String indicatorCode;

        /**指标名称(中文名优先, 无中文名时返回英文名)*/
        @Schema(description = "指标名称(中文名优先, 无中文名时返回英文名)")
        private String indicatorName;
    }

    /**
     * 时序行: 一行代表一个国家
     */
    @Data
    @Schema(description = "时序行: 一行代表一个国家")
    public static class TimeSeriesRow {

        /**国家/经济体代码(ISO3如 CHN, 或聚合经济体编码)*/
        @Schema(description = "国家/经济体代码(ISO3如 CHN, 或聚合经济体编码)")
        private String countryCode;

        /**国家/经济体名称(中文名优先, 无中文名时返回英文名)*/
        @Schema(description = "国家/经济体名称(中文名优先, 无中文名时返回英文名)")
        private String countryName;

        /**数值单元: key=指标编码, value=与yearColumns按索引对齐的数值列表(缺失年份为null)*/
        @Schema(description = "数值单元: key=指标编码, value=与yearColumns按索引对齐的数值列表(缺失年份为null)")
        private Map<String, List<BigDecimal>> values;
    }
}
