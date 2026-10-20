package org.jeecg.modules.weo.service;

import org.jeecg.modules.weo.entity.WeoIndicator;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 世界经济组织指标
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
public interface IWeoIndicatorService extends IService<WeoIndicator> {
    /**
     * 根据指标代码查询指标
     * @param indicatorCode
     * @return
     */
    WeoIndicator findByIndicatorCode(String indicatorCode);
}
