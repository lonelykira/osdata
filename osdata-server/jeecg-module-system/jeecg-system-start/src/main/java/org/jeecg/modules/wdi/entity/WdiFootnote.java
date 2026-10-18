package org.jeecg.modules.wdi.entity;

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
 * @Description: 世行WDI数据脚注表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Data
@TableName("t_wdi_footnote")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世行WDI数据脚注表")
public class WdiFootnote implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
	/**国家代码*/
	@Excel(name = "国家代码", width = 15)
    @Schema(description = "国家代码")
    private String countryCode;
	/**指标代码*/
	@Excel(name = "指标代码", width = 15)
    @Schema(description = "指标代码")
    private String seriesCode;
	/**年份(可为空=全局脚注)*/
	@Excel(name = "年份(可为空=全局脚注)", width = 15)
    @Schema(description = "年份(可为空=全局脚注)")
    private Integer year;
	/**脚注内容(英文)*/
	@Excel(name = "脚注内容(英文)", width = 15)
    @Schema(description = "脚注内容(英文)")
    private String footnoteText;
	/**脚注内容中文(翻译字段)*/
	@Excel(name = "脚注内容中文(翻译字段)", width = 15)
    @Schema(description = "脚注内容中文(翻译字段)")
    private String footnoteTextCn;

}
