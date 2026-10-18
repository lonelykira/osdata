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
 * @Description: 世行WDI指标元数据表(含中文字段)
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Data
@TableName("t_wdi_series")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世行WDI指标元数据表(含中文字段)")
public class WdiSeries implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
	/**指标代码(如 SP.POP.TOTL)*/
	@Excel(name = "指标代码(如 SP.POP.TOTL)", width = 15)
    @Schema(description = "指标代码(如 SP.POP.TOTL)")
    private String seriesCode;
	/**主题(英文)*/
	@Excel(name = "主题(英文)", width = 15)
    @Schema(description = "主题(英文)")
    private String topic;
	/**主题中文(翻译字段)*/
	@Excel(name = "主题中文(翻译字段)", width = 15)
    @Schema(description = "主题中文(翻译字段)")
    private String topicCn;
	/**指标名称(英文)*/
	@Excel(name = "指标名称(英文)", width = 15)
    @Schema(description = "指标名称(英文)")
    private String indicatorName;
	/**指标名称中文(翻译字段)*/
	@Excel(name = "指标名称中文(翻译字段)", width = 15)
    @Schema(description = "指标名称中文(翻译字段)")
    private String indicatorNameCn;
	/**短定义(英文)*/
	@Excel(name = "短定义(英文)", width = 15)
    @Schema(description = "短定义(英文)")
    private String shortDefinition;
	/**短定义中文(翻译字段)*/
	@Excel(name = "短定义中文(翻译字段)", width = 15)
    @Schema(description = "短定义中文(翻译字段)")
    private String shortDefinitionCn;
	/**长定义(英文)*/
	@Excel(name = "长定义(英文)", width = 15)
    @Schema(description = "长定义(英文)")
    private String longDefinition;
	/**长定义中文(翻译字段)*/
	@Excel(name = "长定义中文(翻译字段)", width = 15)
    @Schema(description = "长定义中文(翻译字段)")
    private String longDefinitionCn;
	/**计量单位(英文)*/
	@Excel(name = "计量单位(英文)", width = 15)
    @Schema(description = "计量单位(英文)")
    private String unit;
	/**计量单位中文(翻译字段)*/
	@Excel(name = "计量单位中文(翻译字段)", width = 15)
    @Schema(description = "计量单位中文(翻译字段)")
    private String unitCn;
	/**数据频率(年/季/月)*/
	@Excel(name = "数据频率(年/季/月)", width = 15)
    @Schema(description = "数据频率(年/季/月)")
    private String periodicity;
	/**基期*/
	@Excel(name = "基期", width = 15)
    @Schema(description = "基期")
    private String basePeriod;
	/**其他说明*/
	@Excel(name = "其他说明", width = 15)
    @Schema(description = "其他说明")
    private String otherNotes;
	/**加总方法*/
	@Excel(name = "加总方法", width = 15)
    @Schema(description = "加总方法")
    private String aggregationMethod;
	/**局限与例外*/
	@Excel(name = "局限与例外", width = 15)
    @Schema(description = "局限与例外")
    private String limitationsExceptions;
	/**原始来源备注*/
	@Excel(name = "原始来源备注", width = 15)
    @Schema(description = "原始来源备注")
    private String notesFromOriginalSource;
	/**一般性注释*/
	@Excel(name = "一般性注释", width = 15)
    @Schema(description = "一般性注释")
    private String generalComments;
	/**数据来源机构(英文)*/
	@Excel(name = "数据来源机构(英文)", width = 15)
    @Schema(description = "数据来源机构(英文)")
    private String source;
	/**数据来源机构中文(翻译字段)*/
	@Excel(name = "数据来源机构中文(翻译字段)", width = 15)
    @Schema(description = "数据来源机构中文(翻译字段)")
    private String sourceCn;
	/**统计口径与方法*/
	@Excel(name = "统计口径与方法", width = 15)
    @Schema(description = "统计口径与方法")
    private String statisticalConceptMethodology;
	/**发展相关性*/
	@Excel(name = "发展相关性", width = 15)
    @Schema(description = "发展相关性")
    private String developmentRelevance;
	/**相关来源链接*/
	@Excel(name = "相关来源链接", width = 15)
    @Schema(description = "相关来源链接")
    private String relatedSourceLinks;
	/**其他网页链接*/
	@Excel(name = "其他网页链接", width = 15)
    @Schema(description = "其他网页链接")
    private String otherWebLinks;
	/**关联指标*/
	@Excel(name = "关联指标", width = 15)
    @Schema(description = "关联指标")
    private String relatedIndicators;
	/**许可类型(如 CC BY 4.0)*/
	@Excel(name = "许可类型(如 CC BY 4.0)", width = 15)
    @Schema(description = "许可类型(如 CC BY 4.0)")
    private String licenseType;

}
