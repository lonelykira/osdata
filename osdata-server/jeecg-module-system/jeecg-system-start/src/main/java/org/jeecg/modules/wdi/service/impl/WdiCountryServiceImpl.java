package org.jeecg.modules.wdi.service.impl;

import org.jeecg.modules.wdi.entity.WdiCountry;
import org.jeecg.modules.wdi.mapper.WdiCountryMapper;
import org.jeecg.modules.wdi.service.IWdiCountryService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 世行WDI国家/经济体信息表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Service
public class WdiCountryServiceImpl extends ServiceImpl<WdiCountryMapper, WdiCountry> implements IWdiCountryService {

}
