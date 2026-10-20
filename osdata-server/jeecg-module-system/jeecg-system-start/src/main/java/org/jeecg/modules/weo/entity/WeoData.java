package org.jeecg.modules.weo.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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
 * @Description: 世界经济组织数据表
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@TableName("t_weo_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世界经济组织数据表")
public class WeoData implements Serializable {
    private static final long serialVersionUID = 1L;

	/**自增主键，单条观测记录唯一id*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "自增主键，单条观测记录唯一id")
    private Long id;
	/**冗余存储国家编码，也可join t_weo_country*/
	@Excel(name = "冗余存储国家编码，也可join t_weo_country", width = 15)
    @Schema(description = "冗余存储国家编码，也可join t_weo_country")
    private String countryCode;
	/**冗余存储指标编码，也可join t_weo_indicator*/
	@Excel(name = "冗余存储指标编码，也可join t_weo_indicator", width = 15)
    @Schema(description = "冗余存储指标编码，也可join t_weo_indicator")
    private String indicatorCode;
	/**年份*/
	@Excel(name = "年份", width = 15)
    @Schema(description = "年份")
    private Integer year;
	/**指标数值*/
	@Excel(name = "指标数值", width = 15)
    @Schema(description = "指标数值")
    private BigDecimal value;
}
