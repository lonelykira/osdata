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
 * @Description: 世界经济组织主题
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@TableName("t_weo_topic")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="世界经济组织主题")
public class WeoTopic implements Serializable {
    private static final long serialVersionUID = 1L;

	/**自增主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "自增主键")
    private Long id;
	/**topic英文名称(官方)*/
	@Excel(name = "topic英文名称(官方)", width = 15)
    @Schema(description = "topic英文名称(官方)")
    private String topicEn;
	/**topic中文翻译*/
	@Excel(name = "topic中文翻译", width = 15)
    @Schema(description = "topic中文翻译")
    private String topicCn;
	/**前端展示排序号*/
	@Excel(name = "前端展示排序号", width = 15)
    @Schema(description = "前端展示排序号")
    private Integer sortOrder;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;
}
