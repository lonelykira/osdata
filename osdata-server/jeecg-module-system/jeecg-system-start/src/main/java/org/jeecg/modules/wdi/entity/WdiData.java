package org.jeecg.modules.wdi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 世行WDI面板数据长表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Data
@TableName("t_wdi_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世行WDI面板数据长表")
public class WdiData implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

}
