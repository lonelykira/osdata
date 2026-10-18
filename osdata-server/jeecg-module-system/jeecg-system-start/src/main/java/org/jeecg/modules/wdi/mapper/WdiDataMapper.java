package org.jeecg.modules.wdi.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wdi.entity.WdiData;
import org.jeecg.modules.wdi.entity.WdiFootnote;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 世行WDI面板数据长表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
public interface WdiDataMapper extends BaseMapper<WdiData> {

    /**
     * 面板数据查询: 按国家×指标×年份查t_wdi_data长表
     * SQL走联合唯一索引uk_country_indicator_year(country_code, indicator_code, year)索引range扫描, 禁止全表扫描
     * LIMIT maxRows行数防护(达到上限时由service层标记truncated引导异步导出)
     *
     * @param countryCodes   有效国家编码集合(已过预判过滤)
     * @param indicatorCodes 有效指标编码集合(已过预判过滤)
     * @param startYear      起始年份(可空)
     * @param endYear        结束年份(可空)
     * @param maxRows        返回行数上限
     * @return 长表数据行(按country_code, indicator_code, year排序, 与索引序一致)
     */
    List<WdiData> queryPanelDataRows(@Param("countryCodes") Collection<String> countryCodes,
                                     @Param("indicatorCodes") Collection<String> indicatorCodes,
                                     @Param("startYear") Integer startYear,
                                     @Param("endYear") Integer endYear,
                                     @Param("maxRows") int maxRows);

    /**
     * 脚注查询: 按国家×指标×年份区间关联t_wdi_footnote带出脚注内容
     *
     * @param countryCodes 有效国家编码集合
     * @param seriesCodes  有效指标编码集合
     * @param startYear    起始年份(可空)
     * @param endYear      结束年份(可空)
     * @param maxRows      返回条数上限
     * @return 脚注列表(年份严格按入参区间过滤)
     */
    List<WdiFootnote> queryFootnotes(@Param("countryCodes") Collection<String> countryCodes,
                                     @Param("seriesCodes") Collection<String> seriesCodes,
                                     @Param("startYear") Integer startYear,
                                     @Param("endYear") Integer endYear,
                                     @Param("maxRows") int maxRows);
}
