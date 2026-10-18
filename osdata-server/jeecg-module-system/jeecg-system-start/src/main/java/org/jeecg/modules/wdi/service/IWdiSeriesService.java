package org.jeecg.modules.wdi.service;

import org.jeecg.modules.wdi.entity.WdiSeries;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 世行WDI指标元数据表(含中文字段)
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
public interface IWdiSeriesService extends IService<WdiSeries> {

    /**
     * 按指标编码查询指标元数据
     *
     * @param seriesCode 指标编码(如 SP.POP.TOTL)
     * @return 指标元数据, 不存在返回null
     */
    WdiSeries getSeriesByCode(String seriesCode);

}
