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
 * @Description: 世界经济组织指标
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@TableName("t_weo_indicator")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世界经济组织指标")
public class WeoIndicator implements Serializable {
    private static final long serialVersionUID = 1L;

	/**自增主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "自增主键")
    private Long id;
	/**WEO指标编码，如NGDP,PCPI*/
	@Excel(name = "WEO指标编码，如NGDP,PCPI", width = 15)
    @Schema(description = "WEO指标编码，如NGDP,PCPI")
    private String indicatorCode;
	/**指标英文全称*/
	@Excel(name = "指标英文全称", width = 15)
    @Schema(description = "指标英文全称")
    private String indicatorEn;
	/**指标中文翻译*/
	@Excel(name = "指标中文翻译", width = 15)
    @Schema(description = "指标中文翻译")
    private String indicatorCn;
	/**单位*/
	@Excel(name = "单位", width = 15)
    @Schema(description = "单位")
    private String unit;
	/**量级，Billions/Units等*/
	@Excel(name = "量级，Billions/Units等", width = 15)
    @Schema(description = "量级，Billions/Units等")
    private String scale;
	/**主题编码*/
	@Excel(name = "主题编码", width = 15)
    @Schema(description = "主题编码")
    private String topicCode;
}
