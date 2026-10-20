package org.jeecg.modules.api.weo.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.jeecg.modules.api.weo.dto.CountryBatchDTO;
import org.jeecg.modules.api.weo.dto.CountryPageDTO;
import org.jeecg.modules.api.weo.dto.IndicatorBatchDTO;
import org.jeecg.modules.api.weo.dto.IndicatorPageDTO;
import org.jeecg.modules.api.weo.dto.IndicatorYearRangeDTO;
import org.jeecg.modules.api.weo.dto.TopicPageDTO;
import org.jeecg.modules.api.weo.dto.WeoDataQueryDTO;
import org.jeecg.modules.api.weo.vo.CountryPageVO;
import org.jeecg.modules.api.weo.vo.CountryTreeVO;
import org.jeecg.modules.api.weo.vo.IndicatorDetailVO;
import org.jeecg.modules.api.weo.vo.IndicatorPageVO;
import org.jeecg.modules.api.weo.vo.IndicatorYearRangeVO;
import org.jeecg.modules.api.weo.vo.TopicPageVO;
import org.jeecg.modules.api.weo.vo.TopicTreeVO;
import org.jeecg.modules.api.weo.vo.WeoDataQueryVO;
import org.jeecg.modules.api.weo.vo.WeoStatisticsVO;

/**
 * @Description: WEO数据模块webapi服务(主题Topic + 指标Indicator + 国家Country + 面板数据)
 * 业务逻辑(查询/缓存/树构建/分页/参数校验)全部收敛在本服务及实现中
 * 参数校验异常(JeecgBootException)由全局异常处理器统一转Result.error返回
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
public interface IWeoApiService {

    /**
     * 主题-指标树形目录: 一级节点为Topic主题, 子节点为该主题下的指标列表, 用于前端左侧导航树
     *
     * @return 主题树形目录列表
     */
    List<TopicTreeVO> getTopicTree();

    /**
     * 主题分页列表: 按主题分页, 附带每个主题下的指标数量
     *
     * @param dto 分页入参(pageNo/pageSize)
     * @return 主题分页结果
     */
    IPage<TopicPageVO> queryTopicPage(TopicPageDTO dto);

    /**
     * 指标分页检索: 支持topicCode筛选与名称关键词模糊搜索
     *
     * @param dto 检索入参(pageNo/pageSize/topicCode/keyword)
     * @return 指标元信息分页结果
     */
    IPage<IndicatorPageVO> queryIndicatorPage(IndicatorPageDTO dto);

    /**
     * 指标详情: 返回指标完整定义、单位、量级, 关联t_weo_data返回支持的最小/最大年份
     *
     * @param indicatorCode 指标ID(t_weo_indicator主键)
     * @return 指标详情
     */
    IndicatorDetailVO getIndicatorDetail(String indicatorCode);

    /**
     * 批量获取指标元信息: 查询面板选中指标使用
     *
     * @param dto 批量入参(indicatorCodeList指标编码列表)
     * @return 指标元信息列表(保持入参顺序)
     */
    List<IndicatorPageVO> queryIndicatorBatch(IndicatorBatchDTO dto);

    /**
     * 指标可用国家/经济体清单: 该指标有数据的全部国家与聚合经济体, 按指标编码查询
     *
     * @param indicatorCode 指标编码(如 NGDP/PCPI)
     * @return 国家/经济体清单
     */
    List<CountryPageVO> queryIndicatorCountry(String indicatorCode);

    /**
     * 指标可用年份范围: 查询选中指标各自年份区间(t_weo_data聚合)取交集, 返回交集最小/最大年份
     * 用于前端年份选择框边界限制, 避免用户选到无数据年份
     *
     * @param dto 入参(indicatorCodeList选中指标编码列表)
     * @return 交集年份范围(交集为空时minYear/maxYear返回null)
     */
    IndicatorYearRangeVO queryIndicatorYearRange(IndicatorYearRangeDTO dto);

    /**
     * 国家/经济体分组树: 一级节点为类型分组(单个国家/聚合经济体), 子节点为成员列表, 前端多选组件使用
     *
     * @return 国家/经济体分组树列表
     */
    List<CountryTreeVO> queryCountryTree();

    /**
     * 国家/经济体分页检索: 名称关键词模糊搜索, 支持按类型筛选
     *
     * @param dto 检索入参(pageNo/pageSize/keyword/isAggregate)
     * @return 国家/经济体信息分页结果
     */
    IPage<CountryPageVO> queryCountryPage(CountryPageDTO dto);

    /**
     * 批量获取国家/经济体信息: 查询面板展示使用
     *
     * @param dto 批量入参(countryCodeList编码列表)
     * @return 国家/经济体信息列表(保持入参顺序)
     */
    List<CountryPageVO> queryCountryBatch(CountryBatchDTO dto);

    /**
     * 面板数据查询: 按指标×国家×年份查询t_weo_data面板数据, 支持三种视图
     * TABLE_LONG长表(国家,指标,年份,数值) / TABLE_WIDE宽表透视(Java内存行转列) / CHART图表数据(ECharts结构)
     * 内置大表性能防护: 指标最多10个、国家最多10个(超限直接抛异常), 无效组合预判过滤, 返回上限50000行
     *
     * @param dto 查询入参(indicatorCodeList/countryCodeList必选, startYear/endYear/viewType可选)
     * @return 查询结果(按viewType填充对应视图数据)
     */
    WeoDataQueryVO queryPanelData(WeoDataQueryDTO dto);

    /**
     * WEO统计概览: 返回指标总数、国家/经济体总数、数据年份跨度(min/max)、数据记录总数
     * 优先读Redis缓存(7天), 缓存未命中时回源数据库查询并回写缓存
     *
     * @return 统计概览
     */
    WeoStatisticsVO getStatisticsSummary();
}
