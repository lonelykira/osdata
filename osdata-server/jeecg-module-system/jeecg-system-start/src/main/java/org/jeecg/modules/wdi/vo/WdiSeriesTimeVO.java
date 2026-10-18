package org.jeecg.modules.wdi.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 世行WDI指标年份覆盖表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世行WDI指标年份覆盖表")
public class WdiSeriesTimeVO implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
    @TableId(type = IdType.AUTO)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "主键")
    private Long id;
	/**指标代码*/
	@Excel(name = "指标代码", width = 15)
    @Schema(description = "指标代码")
    private String seriesCode;
	/**年份*/
	@Excel(name = "年份", width = 15)
    @Schema(description = "年份")
    private Integer year;
	/**年份覆盖说明*/
	@Excel(name = "年份覆盖说明", width = 15)
    @Schema(description = "年份覆盖说明")
    private String description;

    @Excel(name = "年份覆盖说明CN", width = 15)
    @Schema(description = "年份覆盖说明CN")
    private String descriptionCn;

}
