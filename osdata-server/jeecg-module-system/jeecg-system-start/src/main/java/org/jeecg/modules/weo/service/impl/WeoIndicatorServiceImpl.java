package org.jeecg.modules.weo.service.impl;

import org.jeecg.modules.weo.entity.WeoIndicator;
import org.jeecg.modules.weo.mapper.WeoIndicatorMapper;
import org.jeecg.modules.weo.service.IWeoIndicatorService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 世界经济组织指标
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Service
public class WeoIndicatorServiceImpl extends ServiceImpl<WeoIndicatorMapper, WeoIndicator> implements IWeoIndicatorService {

    @Override
    public WeoIndicator findByIndicatorCode(String indicatorCode) {
        return lambdaQuery().eq(WeoIndicator::getIndicatorCode, indicatorCode).last("limit 1").one();
    }
}
