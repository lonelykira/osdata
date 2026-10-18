package org.jeecg.modules.wdi.service.impl;

import org.jeecg.modules.wdi.entity.WdiSeries;
import org.jeecg.modules.wdi.mapper.WdiSeriesMapper;
import org.jeecg.modules.wdi.service.IWdiSeriesService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 世行WDI指标元数据表(含中文字段)
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Service
public class WdiSeriesServiceImpl extends ServiceImpl<WdiSeriesMapper, WdiSeries> implements IWdiSeriesService {

    @Override
    public WdiSeries getSeriesByCode(String code) {
        return lambdaQuery().eq(WdiSeries::getSeriesCode, code).last("limit 1").one();
    }
}
