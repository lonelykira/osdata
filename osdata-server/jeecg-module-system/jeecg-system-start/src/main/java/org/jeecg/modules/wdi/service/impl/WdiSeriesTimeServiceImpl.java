package org.jeecg.modules.wdi.service.impl;

import org.jeecg.modules.wdi.entity.WdiSeriesTime;
import org.jeecg.modules.wdi.mapper.WdiSeriesTimeMapper;
import org.jeecg.modules.wdi.service.IWdiSeriesTimeService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 世行WDI指标年份覆盖表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Service
public class WdiSeriesTimeServiceImpl extends ServiceImpl<WdiSeriesTimeMapper, WdiSeriesTime> implements IWdiSeriesTimeService {

}
