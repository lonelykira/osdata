package org.jeecg.modules.api.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.jeecg.modules.api.dto.CountryBatchDTO;
import org.jeecg.modules.api.dto.CountryPageDTO;
import org.jeecg.modules.api.dto.SeriesBatchDTO;
import org.jeecg.modules.api.dto.SeriesPageDTO;
import org.jeecg.modules.api.dto.SeriesYearRangeDTO;
import org.jeecg.modules.api.dto.TopicPageDTO;
import org.jeecg.modules.api.dto.WdiDataQueryDTO;
import org.jeecg.modules.api.vo.CountryPageVO;
import org.jeecg.modules.api.vo.CountryTreeVO;
import org.jeecg.modules.api.vo.SeriesCountryVO;
import org.jeecg.modules.api.vo.SeriesDetailVO;
import org.jeecg.modules.api.vo.SeriesPageVO;
import org.jeecg.modules.api.vo.SeriesYearRangeVO;
import org.jeecg.modules.api.vo.TopicPageVO;
import org.jeecg.modules.api.vo.TopicTreeVO;
import org.jeecg.modules.api.vo.WdiDataQueryVO;
import org.jeecg.modules.api.vo.WdiStatisticsVO;

/**
 * @Description: WDI对外数据服务接口(Topic主题 + 指标series + 国家country + 面板数据data)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
public interface IWdiService {

    /**
     * 获取Topic树形目录: 一级topic, 子节点为该主题下的指标列表, 用于前端左侧导航树
     * 结果存入Redis缓存并设置过期时间, 缓存未命中时查库构建并回写
     *
     * @return Topic树形目录列表
     */
    List<TopicTreeVO> getTopicTree();

    /**
     * Topic分页列表: 按主题去重分页, 附带每个主题下的指标数量
     *
     * @param dto 分页入参(pageNo页码, pageSize每页条数)
     * @return Topic分页结果
     */
    IPage<TopicPageVO> queryTopicPage(TopicPageDTO dto);

    /**
     * 指标分页检索: 支持topic筛选与名称关键词模糊搜索(匹配指标中英文名称及指标代码)
     *
     * @param dto 检索入参(pageNo/pageSize/topicId/keyword)
     * @return 指标元信息分页结果
     */
    IPage<SeriesPageVO> querySeriesPage(SeriesPageDTO dto);

    /**
     * 指标详情: 返回指标完整定义/单位/来源, 并关联t_wdi_series_time返回该指标支持的最小/最大年份
     * 结果存入Redis缓存(1小时过期)
     *
     * @param seriesId 指标ID(t_wdi_series主键)
     * @return 指标详情
     */
    SeriesDetailVO getSeriesDetail(String seriesId);

    /**
     * 批量获取指标元信息: 查询面板选中指标使用, 未命中缓存的指标一次in查询后回写缓存
     *
     * @param dto 批量入参(seriesIdList指标ID列表)
     * @return 指标元信息列表(保持入参顺序)
     */
    List<SeriesPageVO> querySeriesBatch(SeriesBatchDTO dto);

    /**
     * 指标可用年份范围: 查询选中指标各自年份集合(t_wdi_series_time)的交集, 返回交集最小/最大年份
     * 用于前端年份选择框边界限制, 避免用户选无数据年份; 任一指标无年份记录时交集为空(min/max返回null)
     *
     * @param dto 入参(seriesCodeList选中指标编码列表)
     * @return 年份范围(交集最小/最大年份)
     */
    SeriesYearRangeVO querySeriesYearRange(SeriesYearRangeDTO dto);

    /**
     * 获取该指标有数据的全部国家清单: 从t_wdi_country_series关联t_wdi_country查询
     * 结果存入Redis缓存(1小时过期)
     *
     * @param seriesCode 指标编码
     * @return 国家清单(按国家简称排序)
     */
    List<SeriesCountryVO> querySeriesCountry(String seriesCode);

    /**
     * 获取国家分组树: 一级节点为区域region, 子节点为该区域下的国家列表, 附带收入分组标签, 前端多选组件使用
     * 结果存入Redis缓存并设置过期时间, 缓存未命中时查库构建并回写
     *
     * @return 国家分组树列表
     */
    List<CountryTreeVO> queryCountryTree();

    /**
     * 国家分页检索: 名称关键词模糊搜索(匹配国家英文简称/中文名/国家代码)
     *
     * @param dto 检索入参(pageNo/pageSize/keyword)
     * @return 国家信息分页结果
     */
    IPage<CountryPageVO> queryCountryPage(CountryPageDTO dto);

    /**
     * 批量获取国家信息: 查询面板展示使用, 未命中缓存的国家一次in查询后回写缓存
     *
     * @param dto 批量入参(countryIdList国家ID列表)
     * @return 国家信息列表(保持入参顺序)
     */
    List<CountryPageVO> queryCountryBatch(CountryBatchDTO dto);

    /**
     * 面板数据查询: 按指标×国家×年份查询t_wdi_data, 支持三种视图(TABLE_LONG长表/TABLE_WIDE宽表透视/CHART图表)
     * 内置大表性能防护: 指标最多10个、国家最多50个, 无效组合预判过滤, 返回行数上限截断
     *
     * @param dto 查询入参
     * @return 查询结果(按viewType填充对应视图数据)
     */
    WdiDataQueryVO queryPanelData(WdiDataQueryDTO dto);

    /**
     * WDI统计概览: 返回指标总数/国家总数/数据年份跨度/数据记录总数
     * 优先读Redis缓存(应用启动时由WdiStatisticsComponent预加载),
     * 缓存未命中时委托WdiStatisticsComponent回源数据库查询并回写
     *
     * @return 统计概览
     */
    WdiStatisticsVO getStatisticsSummary();
}
