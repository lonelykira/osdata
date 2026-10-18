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
 * @Description: 世行WDI国家/经济体信息表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世行WDI国家/经济体信息表")
public class WdiCountryVO implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
    @TableId(type = IdType.AUTO)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "主键")
    private Long id;
	/**世行国家/经济体代码(ISO3, 如 CHN/WLD)*/
	@Excel(name = "世行国家/经济体代码(ISO3, 如 CHN/WLD)", width = 15)
    @Schema(description = "世行国家/经济体代码(ISO3, 如 CHN/WLD)")
    private String countryCode;
	/**简称(英文)*/
	@Excel(name = "简称(英文)", width = 15)
    @Schema(description = "简称(英文)")
    private String shortName;
	/**国家中文名(翻译字段)*/
	@Excel(name = "国家中文名(翻译字段)", width = 15)
    @Schema(description = "国家中文名(翻译字段)")
    private String countryNameCn;
	/**表名*/
	@Excel(name = "表名", width = 15)
    @Schema(description = "表名")
    private String tableName;
	/**全称(英文)*/
	@Excel(name = "全称(英文)", width = 15)
    @Schema(description = "全称(英文)")
    private String longName;
	/**两位字母代码*/
	@Excel(name = "两位字母代码", width = 15)
    @Schema(description = "两位字母代码")
    private String alpha2Code;
	/**货币单位*/
	@Excel(name = "货币单位", width = 15)
    @Schema(description = "货币单位")
    private String currencyUnit;
	/**特别说明*/
	@Excel(name = "特别说明", width = 15)
    @Schema(description = "特别说明")
    private String specialNotes;
	/**区域(英文)*/
	@Excel(name = "区域(英文)", width = 15)
    @Schema(description = "区域(英文)")
    private String region;
	/**区域中文(翻译字段)*/
	@Excel(name = "区域中文(翻译字段)", width = 15)
    @Schema(description = "区域中文(翻译字段)")
    private String regionCn;
	/**收入分组(英文)*/
	@Excel(name = "收入分组(英文)", width = 15)
    @Schema(description = "收入分组(英文)")
    private String incomeGroup;
	/**收入分组中文(翻译字段)*/
	@Excel(name = "收入分组中文(翻译字段)", width = 15)
    @Schema(description = "收入分组中文(翻译字段)")
    private String incomeGroupCn;
	/**WB-2代码*/
	@Excel(name = "WB-2代码", width = 15)
    @Schema(description = "WB-2代码")
    private String wb2Code;
	/**国民账户基年*/
	@Excel(name = "国民账户基年", width = 15)
    @Schema(description = "国民账户基年")
    private String naBaseYear;
	/**国民账户参考年*/
	@Excel(name = "国民账户参考年", width = 15)
    @Schema(description = "国民账户参考年")
    private String naReferenceYear;
	/**SNA价格核算方式*/
	@Excel(name = "SNA价格核算方式", width = 15)
    @Schema(description = "SNA价格核算方式")
    private String snaPriceValuation;
	/**贷款类别*/
	@Excel(name = "贷款类别", width = 15)
    @Schema(description = "贷款类别")
    private String lendingCategory;
	/**其他分组*/
	@Excel(name = "其他分组", width = 15)
    @Schema(description = "其他分组")
    private String otherGroups;
	/**国民账户体系*/
	@Excel(name = "国民账户体系", width = 15)
    @Schema(description = "国民账户体系")
    private String systemOfNationalAccounts;
	/**备选换算因子*/
	@Excel(name = "备选换算因子", width = 15)
    @Schema(description = "备选换算因子")
    private String alternativeConversionFactor;
	/**PPP调查年份*/
	@Excel(name = "PPP调查年份", width = 15)
    @Schema(description = "PPP调查年份")
    private String pppSurveyYear;
	/**国际收支手册版本*/
	@Excel(name = "国际收支手册版本", width = 15)
    @Schema(description = "国际收支手册版本")
    private String balanceOfPaymentsManual;
	/**外债报告状态*/
	@Excel(name = "外债报告状态", width = 15)
    @Schema(description = "外债报告状态")
    private String externalDebtReportStatus;
	/**贸易体系*/
	@Excel(name = "贸易体系", width = 15)
    @Schema(description = "贸易体系")
    private String systemOfTrade;
	/**政府会计口径*/
	@Excel(name = "政府会计口径", width = 15)
    @Schema(description = "政府会计口径")
    private String governmentAccountingConcept;
	/**IMF数据公布标准*/
	@Excel(name = "IMF数据公布标准", width = 15)
    @Schema(description = "IMF数据公布标准")
    private String imfDataDisseminationStandard;
	/**最近人口普查年份*/
	@Excel(name = "最近人口普查年份", width = 15)
    @Schema(description = "最近人口普查年份")
    private String latestPopulationCensus;
	/**最近住户调查年份*/
	@Excel(name = "最近住户调查年份", width = 15)
    @Schema(description = "最近住户调查年份")
    private String latestHouseholdSurvey;
	/**收入支出数据来源*/
	@Excel(name = "收入支出数据来源", width = 15)
    @Schema(description = "收入支出数据来源")
    private String sourceOfIncomeExpenditureData;
	/**生命登记完整性*/
	@Excel(name = "生命登记完整性", width = 15)
    @Schema(description = "生命登记完整性")
    private String vitalRegistrationComplete;
	/**最近农业普查年份*/
	@Excel(name = "最近农业普查年份", width = 15)
    @Schema(description = "最近农业普查年份")
    private String latestAgriculturalCensus;
	/**最近工业数据年份*/
	@Excel(name = "最近工业数据年份", width = 15)
    @Schema(description = "最近工业数据年份")
    private String latestIndustrialData;
	/**最近贸易数据年份*/
	@Excel(name = "最近贸易数据年份", width = 15)
    @Schema(description = "最近贸易数据年份")
    private String latestTradeData;
	/**最近取水量数据年份*/
	@Excel(name = "最近取水量数据年份", width = 15)
    @Schema(description = "最近取水量数据年份")
    private String latestWaterWithdrawalData;

}
