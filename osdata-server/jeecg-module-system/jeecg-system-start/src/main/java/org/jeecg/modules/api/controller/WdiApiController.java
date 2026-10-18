package org.jeecg.modules.api.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.api.dto.*;
import org.jeecg.modules.api.service.IWdiService;
import org.jeecg.modules.api.vo.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: WDI数据模块接口(对外webapi: Topic主题 + 指标series + 国家country)
 * controller层只负责: 接收参数 -> 调用service方法 -> 返回Result
 * 业务逻辑(查询/缓存/树构建/分页/参数校验)全部在IWdiService及实现中
 * 参数校验异常(JeecgBootException)由全局异常处理器统一转Result.error返回
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Tag(name = "WDI数据接口")
@RestController
@RequestMapping("/webapi/wdi")
@Slf4j
public class WdiApiController {

    @Autowired
    private IWdiService wdiService;

    /**
     * Topic树形目录: 一级节点为Topic主题, 子节点为该主题下的指标列表, 用于前端左侧导航树
     * 结果由service层存入Redis缓存(1小时过期)
     *
     * @return Topic树形目录列表
     */
    @Operation(summary = "Topic树形目录", description = "一级节点为Topic主题, 子节点为指标列表; 结果Redis缓存1小时")
    @GetMapping(value = "/topic/tree")
    public Result<List<TopicTreeVO>> topicTree() {
        return Result.OK(wdiService.getTopicTree());
    }

    /**
     * Topic分页列表: 按主题去重分页, 附带每个主题下的指标数量
     *
     * @param dto 分页入参(pageNo页码, pageSize每页条数)
     * @return Topic分页结果
     */
    @Operation(summary = "Topic分页列表", description = "按主题去重分页, 附带每个主题下的指标数量")
    @GetMapping(value = "/topic/page")
    public Result<IPage<TopicPageVO>> topicPage(TopicPageDTO dto) {
        return Result.OK(wdiService.queryTopicPage(dto));
    }

    /**
     * 指标分页检索: 支持topic筛选与名称关键词模糊搜索
     *
     * @param dto 检索入参(pageNo/pageSize/topicId/keyword)
     * @return 指标元信息分页结果
     */
    @Operation(summary = "指标分页检索", description = "支持topic筛选与名称关键词模糊搜索(匹配指标中英文名称及指标代码)")
    @GetMapping(value = "/series/page")
    public Result<IPage<SeriesPageVO>> seriesPage(SeriesPageDTO dto) {
        return Result.OK(wdiService.querySeriesPage(dto));
    }

    /**
     * 指标详情: 返回指标完整定义、单位、来源, 关联t_wdi_series_time返回支持的最小/最大年份
     *
     * @param seriesId 指标ID(t_wdi_series主键)
     * @return 指标详情
     */
    @Operation(summary = "指标详情", description = "返回指标完整定义、单位、来源及支持的最小/最大年份; 结果Redis缓存1小时")
    @GetMapping(value = "/series/{seriesId}")
    public Result<SeriesDetailVO> seriesDetail(@PathVariable("seriesId") String seriesId) {
        return Result.OK(wdiService.getSeriesDetail(seriesId));
    }

    /**
     * 批量获取指标元信息: 查询面板选中指标使用
     *
     * @param dto 批量入参(seriesIdList指标ID列表)
     * @return 指标元信息列表(保持入参顺序)
     */
    @Operation(summary = "批量获取指标元信息", description = "按指标ID列表批量获取元信息, 查询面板选中指标使用")
    @PostMapping(value = "/series/batch")
    public Result<List<SeriesPageVO>> seriesBatch(@RequestBody SeriesBatchDTO dto) {
        return Result.OK(wdiService.querySeriesBatch(dto));
    }

    /**
     * 指标可用国家清单: 该指标有数据的全部国家, 按指标编码seriesCode查询
     * 从t_wdi_country_series关联t_wdi_country查询
     *
     * @param seriesCode 指标编码(如 SP.POP.TOTL)
     * @return 国家清单(按国家简称排序)
     */
    @Operation(summary = "指标可用国家清单", description = "按指标编码获取该指标有数据的全部国家清单, 按国家简称排序; 结果Redis缓存1小时")
    @GetMapping(value = "/series/{seriesCode}/country")
    public Result<List<SeriesCountryVO>> seriesCountry(@PathVariable("seriesCode") String seriesCode) {
        return Result.OK(wdiService.querySeriesCountry(seriesCode));
    }

    /**
     * 指标可用年份范围: 查询选中指标各自年份集合(t_wdi_series_time)取交集, 返回交集最小/最大年份
     * 用于前端年份选择框边界限制, 避免用户选到无数据年份
     *
     * @param dto 入参(seriesCodeList选中指标编码列表)
     * @return 交集年份范围(交集为空时minYear/maxYear返回null)
     */
    @Operation(summary = "指标可用年份范围", description = "查询选中指标各自年份集合的交集, 返回最小/最大年份, 用于前端年份选择框边界限制")
    @PostMapping(value = "/series/year-range")
    public Result<SeriesYearRangeVO> seriesYearRange(@RequestBody SeriesYearRangeDTO dto) {
        return Result.OK(wdiService.querySeriesYearRange(dto));
    }

    /**
     * 国家分组树: 一级节点为区域region, 子节点为该区域下的国家列表, 附带收入分组标签, 前端多选组件使用
     * 结果由service层存入Redis缓存(1小时过期)
     *
     * @return 国家分组树列表
     */
    @Operation(summary = "国家分组树", description = "按区域分组的国家树, 子节点国家附带收入分组标签, 前端多选组件使用; 结果Redis缓存1小时")
    @GetMapping(value = "/country/tree")
    public Result<List<CountryTreeVO>> countryTree() {
        return Result.OK(wdiService.queryCountryTree());
    }

    /**
     * 国家分页检索: 名称关键词模糊搜索
     *
     * @param dto 检索入参(pageNo/pageSize/keyword)
     * @return 国家信息分页结果
     */
    @Operation(summary = "国家分页检索", description = "支持名称关键词模糊搜索(匹配国家英文简称/中文名/国家代码)")
    @GetMapping(value = "/country/page")
    public Result<IPage<CountryPageVO>> countryPage(CountryPageDTO dto) {
        return Result.OK(wdiService.queryCountryPage(dto));
    }

    /**
     * 批量获取国家信息: 查询面板展示使用, 返回国家名称、编码
     *
     * @param dto 批量入参(countryIdList国家ID列表)
     * @return 国家信息列表(保持入参顺序)
     */
    @Operation(summary = "批量获取国家信息", description = "按国家ID列表批量获取国家信息, 查询面板展示使用")
    @PostMapping(value = "/country/batch")
    public Result<List<CountryPageVO>> countryBatch(@RequestBody CountryBatchDTO dto) {
        return Result.OK(wdiService.queryCountryBatch(dto));
    }

    /**
     * 面板数据查询: 按指标×国家×年份查询t_wdi_data面板数据, 支持三种视图
     * TABLE_LONG长表(国家,指标,年份,数值) / TABLE_WIDE宽表透视(Java内存行转列) / CHART图表数据(ECharts结构)
     * 内置大表性能防护: 指标最多10个、国家最多50个(超限返回提示引导导出), 无效组合预判过滤, 返回上限50000行
     *
     * @param dto 查询入参(seriesCodeList/countryCodeList必选, startYear/endYear/includeFootnote/viewType可选)
     * @return 查询结果(按viewType填充对应视图数据)
     */
    @Operation(summary = "WDI面板数据查询", description = "按指标×国家×年份查询面板数据, 支持TABLE_LONG长表/TABLE_WIDE宽表透视/CHART图表三种视图; "
            + "指标最多10个、国家最多10个, 超限返回提示引导导出; 无效指标-国家组合预判过滤; 结果上限50000行防OOM, 截断时truncated=true建议异步导出")
    @PostMapping(value = "/data/query")
    public Result<WdiDataQueryVO> query(@RequestBody WdiDataQueryDTO dto) {
        return Result.OK(wdiService.queryPanelData(dto));
    }

    /**
     * WDI统计概览: 返回指标总数、国家总数、数据年份跨度(min/max)、数据记录总数
     * 应用启动时由WdiStatisticsComponent通过CompletableFuture多线程查询并预加载到Redis,
     * 本接口优先读Redis缓存, 缓存为空(未预加载/已过期)时回源数据库查询并回写缓存
     *
     * @return 统计概览
     */
    @Operation(summary = "WDI统计概览", description = "返回指标总数/国家总数/数据年份跨度/数据记录总数; "
            + "应用启动时预加载到Redis, 本接口优先读缓存, 缓存为空时回源数据库查询并回写")
    @GetMapping(value = "/statisticsSummary")
    public Result<WdiStatisticsVO> statisticsSummary() {
        return Result.OK(wdiService.getStatisticsSummary());
    }
}
