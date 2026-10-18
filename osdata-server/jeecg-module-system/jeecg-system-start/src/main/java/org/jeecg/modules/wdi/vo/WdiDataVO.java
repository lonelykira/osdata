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
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 世行WDI面板数据长表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世行WDI面板数据长表")
public class WdiDataVO implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "主键")
    private Long id;

	/**国家代码(ISO3, 关联wdi_country)*/
	@Excel(name = "国家代码(ISO3, 关联wdi_country)", width = 15)
    @Schema(description = "国家代码(ISO3, 关联wdi_country)")
    private String countryCode;

	/**指标代码(关联wdi_series)*/
	@Excel(name = "指标代码(关联wdi_series)", width = 15)
    @Schema(description = "指标代码(关联wdi_series)")
    private String indicatorCode;

	/**年份(1960-2025)*/
	@Excel(name = "年份(1960-2025)", width = 15)
    @Schema(description = "年份(1960-2025)")
    private Integer year;
	/**指标数值(缺失为NULL)*/
	@Excel(name = "指标数值(缺失为NULL)", width = 15)
    @Schema(description = "指标数值(缺失为NULL)")
    private BigDecimal value;


}
