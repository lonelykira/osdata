package org.jeecg.modules.weo.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.weo.entity.WeoData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 世界经济组织数据表
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
public interface WeoDataMapper extends BaseMapper<WeoData> {

    /**
     * 面板数据查询: 按国家×指标×年份查t_weo_data长表
     * SQL走联合唯一索引uk_obs_unique(country_code, indicator_code, year)索引range扫描, 禁止全表扫描
     * LIMIT maxRows行数防护(达到上限时由service层标记truncated引导异步导出)
     *
     * @param countryCodes   有效国家编码集合(已过预判过滤)
     * @param indicatorCodes 有效指标编码集合(已过预判过滤)
     * @param startYear      起始年份(可空)
     * @param endYear        结束年份(可空)
     * @param maxRows        返回行数上限
     * @return 长表数据行(按country_code, indicator_code, year排序, 与索引序一致)
     */
    List<WeoData> queryPanelDataRows(@Param("countryCodes") Collection<String> countryCodes,
                                     @Param("indicatorCodes") Collection<String> indicatorCodes,
                                     @Param("startYear") Integer startYear,
                                     @Param("endYear") Integer endYear,
                                     @Param("maxRows") int maxRows);
}
