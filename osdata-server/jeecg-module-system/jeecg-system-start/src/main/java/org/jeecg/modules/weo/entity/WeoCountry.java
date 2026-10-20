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
 * @Description: 世界经济组织国家
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@TableName("t_weo_country")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世界经济组织国家")
public class WeoCountry implements Serializable {
    private static final long serialVersionUID = 1L;

	/**自增主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "自增主键")
    private Long id;
	/**WEO国家编码，如CHN,USA*/
	@Excel(name = "WEO国家编码，如CHN,USA", width = 15)
    @Schema(description = "WEO国家编码，如CHN,USA")
    private String countryCode;
	/**国家英文名称*/
	@Excel(name = "国家英文名称", width = 15)
    @Schema(description = "国家英文名称")
    private String countryEn;
	/**国家中文名称*/
	@Excel(name = "国家中文名称", width = 15)
    @Schema(description = "国家中文名称")
    private String countryCn;
	/**0=单个国家 1=聚合经济体/区域分组*/
	@Excel(name = "0=单个国家 1=聚合经济体/区域分组", width = 15)
    @Schema(description = "0=单个国家 1=聚合经济体/区域分组")
    private Integer isAggregate;
}
