package org.jeecg.modules.wdi.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wdi.entity.WdiSeries;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 世行WDI指标元数据表(含中文字段)
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
public interface WdiSeriesMapper extends BaseMapper<WdiSeries> {

}
