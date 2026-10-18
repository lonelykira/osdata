package org.jeecg.modules.api.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.api.component.WdiStatisticsComponent;
import org.jeecg.modules.api.dto.CountryBatchDTO;
import org.jeecg.modules.api.dto.CountryPageDTO;
import org.jeecg.modules.api.dto.SeriesBatchDTO;
import org.jeecg.modules.api.dto.SeriesPageDTO;
import org.jeecg.modules.api.dto.SeriesYearRangeDTO;
import org.jeecg.modules.api.dto.TopicPageDTO;
import org.jeecg.modules.api.dto.WdiDataQueryDTO;
import org.jeecg.modules.api.service.IWdiService;
import org.jeecg.modules.api.vo.CountryPageVO;
import org.jeecg.modules.api.vo.CountryTreeVO;
import org.jeecg.modules.api.vo.SeriesCountryVO;
import org.jeecg.modules.api.vo.SeriesDetailVO;
import org.jeecg.modules.api.vo.SeriesPageVO;
import org.jeecg.modules.api.vo.SeriesYearRangeVO;
import org.jeecg.modules.api.vo.TopicPageVO;
import org.jeecg.modules.api.vo.TopicTreeVO;
import org.jeecg.modules.api.vo.WdiDataChartVO;
import org.jeecg.modules.api.vo.WdiDataFootnoteVO;
import org.jeecg.modules.api.vo.WdiDataLongRowVO;
import org.jeecg.modules.api.vo.WdiDataQueryVO;
import org.jeecg.modules.api.vo.WdiDataWideVO;
import org.jeecg.modules.api.vo.WdiStatisticsVO;
import org.jeecg.modules.api.vo.WdiSeriesTimeVO;
import org.jeecg.modules.wdi.entity.WdiCountry;
import org.jeecg.modules.wdi.entity.WdiData;
import org.jeecg.modules.wdi.entity.WdiFootnote;
import org.jeecg.modules.wdi.entity.WdiSeries;
import org.jeecg.modules.wdi.entity.WdiSeriesTime;
import org.jeecg.modules.wdi.mapper.WdiDataMapper;
import org.jeecg.modules.wdi.service.IWdiCountryService;
import org.jeecg.modules.wdi.service.IWdiSeriesService;
import org.jeecg.modules.wdi.service.IWdiSeriesTimeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: WDI对外数据服务实现(Topic主题 + 指标series + 国家country + 面板数据data)
 * 约定: 参数校验不通过抛JeecgBootException, 由全局异常处理器统一转Result.error返回
 * 面板数据查询(t_wdi_data约900w行)性能防护设计:
 * 1.参数强校验: 指标最多10个、国家最多10个, 超限直接拒绝并引导导出;
 * 2.查询前预判: 先查t_wdi_data实际数据过滤无效【指标-国家】组合, 全部无效时短路返回不扫大表;
 * 3.SQL走联合唯一索引uk_country_indicator_year(country_code, indicator_code, year)索引range扫描, 禁止全表扫描;
 * 4.返回行数上限MAX_RETURN_ROWS, SQL层LIMIT截断, 结果按country,indicator,year排序与索引序一致无filesort;
 * 5.TABLE_WIDE宽表透视在Java内存完成, 禁止动态SQL行转列。
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Slf4j
@Service
public class WdiServiceImpl implements IWdiService {

    /**Topic树形目录的Redis缓存key*/
    private static final String TOPIC_TREE_CACHE_KEY = "wdi:topic:tree";
    /**指标详情Redis缓存key前缀*/
    private static final String SERIES_DETAIL_CACHE_KEY = "wdi:series:detail:";
    /**指标元信息Redis缓存key前缀(批量接口逐指标缓存)*/
    private static final String SERIES_META_CACHE_KEY = "wdi:series:meta:";
    /**指标可用国家清单Redis缓存key前缀*/
    private static final String SERIES_COUNTRY_CACHE_KEY = "wdi:series:country:";
    /**国家分组树Redis缓存key*/
    private static final String COUNTRY_TREE_CACHE_KEY = "wdi:country:tree";
    /**国家元信息Redis缓存key前缀(批量接口逐国家缓存)*/
    private static final String COUNTRY_META_CACHE_KEY = "wdi:country:meta:";
    /**元数据缓存过期时间: 1小时(单位秒)*/
    private static final long CACHE_SECONDS = 3600L;
    /**分页每页条数上限, 防止大分页拖垮数据库*/
    private static final int MAX_PAGE_SIZE = 100;
    /**面板视图类型: 长表(国家,指标,年份,数值)*/
    public static final String VIEW_TYPE_TABLE_LONG = "TABLE_LONG";
    /**面板视图类型: 宽表透视(行=国家×指标, 列=年份)*/
    public static final String VIEW_TYPE_TABLE_WIDE = "TABLE_WIDE";
    /**面板视图类型: 图表数据(ECharts折线/柱状图)*/
    public static final String VIEW_TYPE_CHART = "CHART";
    /**面板查询指标数量上限, 超过引导使用导出功能*/
    private static final int MAX_SERIES_COUNT = 10;
    /**面板查询国家数量上限, 超过引导使用导出功能*/
    private static final int MAX_COUNTRY_COUNT = 10;
    /**面板查询单次返回数据行数上限, 防止OOM; 达到上限标记truncated=true引导异步导出*/
    private static final int MAX_RETURN_ROWS = 50000;
    /**面板查询脚注返回条数上限(脚注为小表, 防御性限制)*/
    private static final int MAX_FOOTNOTE_ROWS = 5000;
    /**面板宽表/图表行分组key的分隔符(代码中不含此字符)*/
    private static final String GROUP_KEY_SEPARATOR = "||";

    /**统计信息组件: 承担统计概览的回源查询(CompletableFuture并行)与启动预加载, 统计缓存key见WdiStatisticsComponent.STATISTICS_CACHE_KEY*/
    @Autowired
    private WdiStatisticsComponent wdiStatisticsComponent;

    @Autowired
    private IWdiSeriesService wdiSeriesService;
    @Autowired
    private IWdiSeriesTimeService wdiSeriesTimeService;
    @Autowired
    private IWdiCountryService wdiCountryService;
    @Autowired
    private RedisUtil redisUtil;

    @Resource
    private WdiDataMapper wdiDataMapper;

    /**
     * 获取Topic树形目录
     * 查询流程: 优先读Redis缓存 -> 未命中查库构建树 -> 序列化JSON回写缓存(1小时过期)
     */
    @Override
    public List<TopicTreeVO> getTopicTree() {
        // 1. 优先读取Redis缓存(缓存值为JSON字符串, 规避反序列化类型问题)
        Object cache = redisUtil.get(TOPIC_TREE_CACHE_KEY);
        if (cache != null) {
            return JSON.parseArray(cache.toString(), TopicTreeVO.class);
        }
        // 2. 缓存未命中: 查询指标元数据(仅取树所需字段), 按主题+指标代码排序保证树结构稳定
        QueryWrapper<WdiSeries> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("topic", "topic_cn", "series_code", "indicator_name", "indicator_name_cn")
                .isNotNull("topic").ne("topic", "")
                .orderByAsc("topic").orderByAsc("series_code");
        List<WdiSeries> seriesList = wdiSeriesService.list(queryWrapper);
        // 3. 按topic分组构建树(LinkedHashMap保持主题的查询顺序)
        Map<String, TopicTreeVO> topicMap = new LinkedHashMap<>();
        for (WdiSeries series : seriesList) {
            TopicTreeVO node = topicMap.computeIfAbsent(series.getTopic(), key -> {
                TopicTreeVO vo = new TopicTreeVO();
                vo.setTopic(key);
                vo.setTopicCn(series.getTopicCn());
                vo.setChildren(new ArrayList<>());
                return vo;
            });
            TopicTreeVO.IndicatorNode leaf = new TopicTreeVO.IndicatorNode();
            leaf.setSeriesCode(series.getSeriesCode());
            leaf.setIndicatorName(series.getIndicatorName());
            leaf.setIndicatorNameCn(series.getIndicatorNameCn());
            node.getChildren().add(leaf);
        }
        List<TopicTreeVO> tree = new ArrayList<>(topicMap.values());
        // 4. 结果序列化为JSON字符串写入Redis, 并设置过期时间
        redisUtil.set(TOPIC_TREE_CACHE_KEY, JSON.toJSONString(tree), CACHE_SECONDS);
        return tree;
    }

    /**
     * Topic分页列表
     * 查询流程: 主题分页(DISTINCT) -> 一次group by查全量指标数 -> 内存组装VO, 避免N+1查询
     */
    @Override
    public IPage<TopicPageVO> queryTopicPage(TopicPageDTO dto) {
        // 1. 分页参数校验与兜底(页码最小1, 每页条数限制在1~100)
        int pageNo = (dto == null || dto.getPageNo() == null || dto.getPageNo() < 1) ? 1 : dto.getPageNo();
        int pageSize = (dto == null || dto.getPageSize() == null || dto.getPageSize() < 1) ? 10 : Math.min(dto.getPageSize(), MAX_PAGE_SIZE);
        // 2. 主题去重分页查询(MyBatis-Plus分页插件自动生成count语句)
        QueryWrapper<WdiSeries> pageWrapper = new QueryWrapper<>();
        pageWrapper.select("DISTINCT topic", "topic_cn")
                .isNotNull("topic").ne("topic", "")
                .orderByAsc("topic");
        Page<WdiSeries> page = wdiSeriesService.page(new Page<>(pageNo, pageSize), pageWrapper);
        // 3. 一次group by查出各主题的指标数量, 避免在分页结果里逐条查库(N+1问题)
        QueryWrapper<WdiSeries> countWrapper = new QueryWrapper<>();
        countWrapper.select("topic", "COUNT(*) AS cnt")
                .isNotNull("topic").ne("topic", "")
                .groupBy("topic");
        Map<String, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : wdiSeriesService.listMaps(countWrapper)) {
            Object topic = row.get("topic");
            Object cnt = row.get("cnt");
            if (topic != null) {
                countMap.put(topic.toString(), cnt == null ? 0L : Long.valueOf(cnt.toString()));
            }
        }
        // 4. 组装分页结果VO(保留MP分页的total/size/current信息)
        IPage<TopicPageVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<TopicPageVO> records = new ArrayList<>();
        for (WdiSeries series : page.getRecords()) {
            TopicPageVO vo = new TopicPageVO();
            vo.setTopic(series.getTopic());
            vo.setTopicCn(series.getTopicCn());
            vo.setIndicatorCount(countMap.getOrDefault(series.getTopic(), 0L));
            records.add(vo);
        }
        result.setRecords(records);
        return result;
    }

    /**
     * 指标分页检索
     * 支持topic精确筛选与keyword模糊搜索(指标中英文名称/指标代码), 全部MyBatis-Plus查询
     */
    @Override
    public IPage<SeriesPageVO> querySeriesPage(SeriesPageDTO dto) {
        // 1. 分页参数校验与兜底(页码最小1, 每页条数限制在1~100)
        int pageNo = (dto == null || dto.getPageNo() == null || dto.getPageNo() < 1) ? 1 : dto.getPageNo();
        int pageSize = (dto == null || dto.getPageSize() == null || dto.getPageSize() < 1) ? 10 : Math.min(dto.getPageSize(), MAX_PAGE_SIZE);
        // 2. 构建查询条件: topic精确筛选 + keyword模糊搜索(OR组合, 括号避免污染外层条件)
        QueryWrapper<WdiSeries> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "series_code", "indicator_name", "indicator_name_cn", "topic", "topic_cn",
                        "unit", "unit_cn", "periodicity", "source", "source_cn")
                .isNotNull("topic").ne("topic", "");
        if (StringUtils.isNotBlank(dto.getTopicCode())) {
            queryWrapper.eq("topic", dto.getTopicCode().trim());
        }
        if (dto != null && StringUtils.isNotBlank(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like("indicator_name", keyword)
                    .or().like("indicator_name_cn", keyword)
                    .or().like("series_code", keyword));
        }
        queryWrapper.orderByAsc("topic").orderByAsc("series_code");
        // 3. 分页查询(MyBatis-Plus分页插件自动生成count语句)并转换为VO
        Page<WdiSeries> page = wdiSeriesService.page(new Page<>(pageNo, pageSize), queryWrapper);
        IPage<SeriesPageVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<SeriesPageVO> records = new ArrayList<>();
        for (WdiSeries series : page.getRecords()) {
            records.add(convertSeriesPageVO(series));
        }
        result.setRecords(records);
        return result;
    }

    /**
     * 指标详情
     * 查询流程: 参数校验 -> 读Redis缓存 -> 未命中查主表+t_wdi_series_time聚合min/max年份 -> 回写缓存
     */
    @Override
    public SeriesDetailVO getSeriesDetail(String seriesId) {
        // 1. 参数校验: id为空返回参数错误
        Long id = checkAndParseId(seriesId, "指标ID不能为空");
        // 2. 优先读取Redis缓存
        String cacheKey = SERIES_DETAIL_CACHE_KEY + id;
        Object cache = redisUtil.get(cacheKey);
        if (cache != null) {
            return JSON.parseObject(cache.toString(), SeriesDetailVO.class);
        }
        // 3. 查询指标主表, 不存在抛业务异常
        WdiSeries series = wdiSeriesService.getById(id);
        if (series == null) {
            throw new JeecgBootException("指标不存在: " + id);
        }
        // 4. 关联t_wdi_series_time一次聚合查出该指标支持的最小/最大年份
        QueryWrapper<WdiSeriesTime> timeWrapper = new QueryWrapper<>();
        timeWrapper.select("MIN(year) AS minYear", "MAX(year) AS maxYear")
                .eq("series_code", series.getSeriesCode());
        Integer minYear = null;
        Integer maxYear = null;
        // 注意: 聚合无匹配行时MIN/MAX返回一行全NULL, MyBatis listMaps对整行NULL结果返回null元素, 必须跳过
        for (Map<String, Object> row : wdiSeriesTimeService.listMaps(timeWrapper)) {
            if (row == null) {
                continue;
            }
            Object min = row.get("minYear");
            Object max = row.get("maxYear");
            if (min != null) {
                minYear = Integer.valueOf(min.toString());
            }
            if (max != null) {
                maxYear = Integer.valueOf(max.toString());
            }
        }
        // 5. 组装详情VO并回写Redis缓存(1小时过期)
        SeriesDetailVO vo = convertSeriesDetailVO(series);
        vo.setMinYear(minYear);
        vo.setMaxYear(maxYear);
        redisUtil.set(cacheKey, JSON.toJSONString(vo), CACHE_SECONDS);
        return vo;
    }

    /**
     * 批量获取指标元信息(查询面板选中指标使用)
     * 查询流程: 参数校验 -> 逐指标读meta缓存 -> 未命中的收集后一次in查询 -> 回写缓存 -> 按入参顺序输出
     */
    @Override
    public List<SeriesPageVO> querySeriesBatch(SeriesBatchDTO dto) {
        // 1. 参数校验: ID列表为空返回参数错误
        if (CollUtil.isEmpty(dto.getSeriesCodeList())) {
            throw new JeecgBootException("指标编码列表不能为空");
        }
        // 2. ID转Long并校验格式
        List<Long> idList = new ArrayList<>();
        for (String seriesId : dto.getSeriesCodeList()) {
            idList.add(checkAndParseId(seriesId, "指标ID不能为空"));
        }
        // 3. 逐指标读缓存, 未命中的收集待查
        List<SeriesPageVO> resultList = new ArrayList<>(idList.size());
        Map<Long, SeriesPageVO> voMap = new HashMap<>();
        List<Long> missIds = new ArrayList<>();
        for (Long id : idList) {
            if (voMap.containsKey(id)) {
                continue;
            }
            Object cache = redisUtil.get(SERIES_META_CACHE_KEY + id);
            if (cache != null) {
                voMap.put(id, JSON.parseObject(cache.toString(), SeriesPageVO.class));
            } else {
                missIds.add(id);
            }
        }
        // 4. 未命中的指标一次in查询后逐个回写缓存
        if (!missIds.isEmpty()) {
            QueryWrapper<WdiSeries> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id", "series_code", "indicator_name", "indicator_name_cn", "topic", "topic_cn",
                            "unit", "unit_cn", "periodicity", "source", "source_cn")
                    .in("id", missIds);
            for (WdiSeries series : wdiSeriesService.list(queryWrapper)) {
                SeriesPageVO vo = convertSeriesPageVO(series);
                voMap.put(series.getId(), vo);
                redisUtil.set(SERIES_META_CACHE_KEY + series.getId(), JSON.toJSONString(vo), CACHE_SECONDS);
            }
        }
        // 5. 按入参顺序组装输出, 库中不存在的指标跳过
        for (Long id : idList) {
            SeriesPageVO vo = voMap.get(id);
            if (vo != null) {
                resultList.add(vo);
            }
        }
        return resultList;
    }

    /**
     * 指标可用年份范围
     * 查询流程: 参数校验 -> 一次in查询选中指标全部年份记录(t_wdi_series_time) -> 按指标分组 -> 逐年集合求交集 -> 取交集最小/最大年份
     * 任一指标无年份记录时交集为空(min/max返回null), 由前端自行处理
     */
    @Override
    public SeriesYearRangeVO querySeriesYearRange(SeriesYearRangeDTO dto) {
        // 1. 参数校验: 编码数组不能为空
        if (dto == null || dto.getSeriesCodeList() == null || dto.getSeriesCodeList().isEmpty()) {
            throw new JeecgBootException("指标编码列表不能为空");
        }
        // 2. 入参去重(保持顺序)并校验元素非空
        Set<String> codeSet = new LinkedHashSet<>();
        for (String code : dto.getSeriesCodeList()) {
            if (StringUtils.isBlank(code)) {
                throw new JeecgBootException("指标编码不能为空");
            }
            codeSet.add(code.trim());
        }
        // 3. 一次in查询所有选中指标的年份记录(仅取series_code/year两列, 底层为WdiSeriesTimeMapper)
        QueryWrapper<WdiSeriesTime> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("series_code", "year").in("series_code", codeSet);
        Map<String, Set<Integer>> yearMap = new HashMap<>();
        for (WdiSeriesTime seriesTime : wdiSeriesTimeService.list(queryWrapper)) {
            if (seriesTime.getYear() != null) {
                yearMap.computeIfAbsent(seriesTime.getSeriesCode(), key -> new HashSet<>()).add(seriesTime.getYear());
            }
        }
        // 4. 逐指标求年份交集: 任一指标无年份记录则交集为空(该指标任何年份都无数据)
        Set<Integer> intersection = null;
        for (String code : codeSet) {
            Set<Integer> years = yearMap.get(code);
            if (years == null || years.isEmpty()) {
                log.warn("指标[{}]在t_wdi_series_time中无年份记录, 年份交集为空", code);
                intersection = Collections.emptySet();
                break;
            }
            if (intersection == null) {
                intersection = new HashSet<>(years);
            } else {
                intersection.retainAll(years);
            }
        }
        // 5. 组装结果: 交集为空时min/max返回null
        SeriesYearRangeVO vo = new SeriesYearRangeVO();
        if (intersection != null && !intersection.isEmpty()) {
            vo.setMinYear(Collections.min(intersection));
            vo.setMaxYear(Collections.max(intersection));
        }
        return vo;
    }

    /**
     * 获取该指标有数据的全部国家清单
     * 查询流程: 参数校验+指标存在性校验 -> 读Redis缓存 -> 未命中查t_wdi_data实际数据(去重国家) -> in关联t_wdi_country -> 回写缓存
     */
    @Override
    public List<SeriesCountryVO> querySeriesCountry(String seriesCode) {
        // 1. 参数校验: 指标编码为空返回参数错误
        if (StringUtils.isBlank(seriesCode)) {
            throw new JeecgBootException("指标编码不能为空");
        }
        // 2. 优先读取Redis缓存
        String cacheKey = SERIES_COUNTRY_CACHE_KEY + seriesCode;
        Object cache = redisUtil.get(cacheKey);
        if (cache != null) {
            return JSON.parseArray(cache.toString(), SeriesCountryVO.class);
        }
        // 3. 校验指标存在性并获取指标代码
        WdiSeries series = wdiSeriesService.getSeriesByCode(seriesCode);
        if (series == null) {
            throw new JeecgBootException("指标编码不存在: " + seriesCode);
        }
        // 4. 查询该指标有数据的国家代码(以t_wdi_data实际数据为准, t_wdi_country_series与实际数据存在错位不可用)
        QueryWrapper<WdiData> dataWrapper = new QueryWrapper<>();
        dataWrapper.select("DISTINCT country_code").eq("indicator_code", series.getSeriesCode());
        List<String> countryCodes = new ArrayList<>();
        for (WdiData data : wdiDataMapper.selectList(dataWrapper)) {
            if (StringUtils.isNotBlank(data.getCountryCode())) {
                countryCodes.add(data.getCountryCode());
            }
        }
        if (countryCodes.isEmpty()) {
            return Collections.emptyList();
        }
        // 5. 批量in关联t_wdi_country查询国家信息(避免逐个查库), 按国家简称排序
        QueryWrapper<WdiCountry> countryWrapper = new QueryWrapper<>();
        countryWrapper.select("country_code", "short_name", "country_name_cn")
                .in("country_code", countryCodes)
                .orderByAsc("short_name");
        List<SeriesCountryVO> countryList = new ArrayList<>();
        for (WdiCountry country : wdiCountryService.list(countryWrapper)) {
            SeriesCountryVO vo = new SeriesCountryVO();
            vo.setCountryCode(country.getCountryCode());
            vo.setShortName(country.getShortName());
            vo.setCountryNameCn(country.getCountryNameCn());
            countryList.add(vo);
        }
        // 6. 回写Redis缓存(1小时过期)
        redisUtil.set(cacheKey, JSON.toJSONString(countryList), CACHE_SECONDS);
        return countryList;
    }

    /**
     * 参数校验: 指标ID为空抛参数错误, 非数字抛格式错误
     *
     * @param seriesId    指标ID字符串
     * @param emptyMessage 为空时的错误提示
     * @return Long类型的指标ID
     */
    private Long checkAndParseId(String seriesId, String emptyMessage) {
        if (StringUtils.isBlank(seriesId)) {
            throw new JeecgBootException(emptyMessage);
        }
        try {
            return Long.valueOf(seriesId.trim());
        } catch (NumberFormatException e) {
            throw new JeecgBootException("指标ID格式错误: " + seriesId);
        }
    }

    /**
     * WdiSeries实体转指标元信息VO
     */
    private SeriesPageVO convertSeriesPageVO(WdiSeries series) {
        SeriesPageVO vo = new SeriesPageVO();
        vo.setId(series.getId());
        vo.setSeriesCode(series.getSeriesCode());
        vo.setIndicatorName(series.getIndicatorName());
        vo.setIndicatorNameCn(series.getIndicatorNameCn());
        vo.setTopic(series.getTopic());
        vo.setTopicCn(series.getTopicCn());
        vo.setUnit(series.getUnit());
        vo.setUnitCn(series.getUnitCn());
        vo.setPeriodicity(series.getPeriodicity());
        vo.setSource(series.getSource());
        vo.setSourceCn(series.getSourceCn());
        return vo;
    }

    /**
     * WdiSeries实体转指标详情VO
     */
    private SeriesDetailVO convertSeriesDetailVO(WdiSeries series) {
        SeriesDetailVO vo = new SeriesDetailVO();
        vo.setId(series.getId());
        vo.setSeriesCode(series.getSeriesCode());
        vo.setIndicatorName(series.getIndicatorName());
        vo.setIndicatorNameCn(series.getIndicatorNameCn());
        vo.setTopic(series.getTopic());
        vo.setTopicCn(series.getTopicCn());
        vo.setShortDefinition(series.getShortDefinition());
        vo.setShortDefinitionCn(series.getShortDefinitionCn());
        vo.setLongDefinition(series.getLongDefinition());
        vo.setLongDefinitionCn(series.getLongDefinitionCn());
        vo.setUnit(series.getUnit());
        vo.setUnitCn(series.getUnitCn());
        vo.setPeriodicity(series.getPeriodicity());
        vo.setBasePeriod(series.getBasePeriod());
        vo.setAggregationMethod(series.getAggregationMethod());
        vo.setSource(series.getSource());
        vo.setSourceCn(series.getSourceCn());
        vo.setRelatedSourceLinks(series.getRelatedSourceLinks());
        vo.setLicenseType(series.getLicenseType());
        return vo;
    }

    /**
     * 获取国家分组树(按区域region分组, 子节点国家附带收入分组标签, 前端多选组件使用)
     * 查询流程: 优先读Redis缓存 -> 未命中查全部国家按区域分组构建树 -> 回写缓存(1小时过期)
     */
    @Override
    public List<CountryTreeVO> queryCountryTree() {
        // 1. 优先读取Redis缓存
        Object cache = redisUtil.get(COUNTRY_TREE_CACHE_KEY);
        if (cache != null) {
            return JSON.parseArray(cache.toString(), CountryTreeVO.class);
        }
        // 2. 查询全部国家(仅取树所需字段), 按区域+国家简称排序保证树结构稳定
        QueryWrapper<WdiCountry> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "country_code", "short_name", "country_name_cn", "region", "region_cn", "income_group", "income_group_cn")
                .orderByAsc("region").orderByAsc("short_name");
        List<WdiCountry> countryList = wdiCountryService.list(queryWrapper);
        // 3. 按region分组构建树(LinkedHashMap保持区域顺序; region为空归入Other/其他)
        Map<String, CountryTreeVO> regionMap = new LinkedHashMap<>();
        for (WdiCountry country : countryList) {
            String regionKey = StringUtils.isNotBlank(country.getRegion()) ? country.getRegion() : "Other";
            String regionCnValue = StringUtils.isNotBlank(country.getRegionCn()) ? country.getRegionCn() : "其他";
            CountryTreeVO node = regionMap.computeIfAbsent(regionKey, key -> {
                CountryTreeVO vo = new CountryTreeVO();
                vo.setRegion(key);
                vo.setRegionCn(regionCnValue);
                vo.setChildren(new ArrayList<>());
                return vo;
            });
            CountryTreeVO.CountryNode leaf = new CountryTreeVO.CountryNode();
            leaf.setId(country.getId());
            leaf.setCountryCode(country.getCountryCode());
            leaf.setShortName(country.getShortName());
            leaf.setCountryNameCn(country.getCountryNameCn());
            leaf.setIncomeGroup(country.getIncomeGroup());
            leaf.setIncomeGroupCn(country.getIncomeGroupCn());
            node.getChildren().add(leaf);
        }
        List<CountryTreeVO> tree = new ArrayList<>(regionMap.values());
        // 4. 结果序列化为JSON字符串写入Redis, 并设置过期时间
        redisUtil.set(COUNTRY_TREE_CACHE_KEY, JSON.toJSONString(tree), CACHE_SECONDS);
        return tree;
    }

    /**
     * 国家分页检索(名称关键词模糊搜索, 匹配国家英文简称/中文名/国家代码)
     */
    @Override
    public IPage<CountryPageVO> queryCountryPage(CountryPageDTO dto) {
        // 1. 分页参数校验与兜底(页码最小1, 每页条数限制在1~100)
        int pageNo = (dto == null || dto.getPageNo() == null || dto.getPageNo() < 1) ? 1 : dto.getPageNo();
        int pageSize = (dto == null || dto.getPageSize() == null || dto.getPageSize() < 1) ? 10 : Math.min(dto.getPageSize(), MAX_PAGE_SIZE);
        // 2. 构建查询条件: keyword模糊搜索(OR组合, 括号避免污染外层条件)
        QueryWrapper<WdiCountry> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "country_code", "short_name", "country_name_cn", "region", "region_cn", "income_group", "income_group_cn");
        if (dto != null && StringUtils.isNotBlank(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like("short_name", keyword)
                    .or().like("country_name_cn", keyword)
                    .or().like("country_code", keyword));
        }
        queryWrapper.orderByAsc("short_name");
        // 3. 分页查询(MyBatis-Plus分页插件自动生成count语句)并转换为VO
        Page<WdiCountry> page = wdiCountryService.page(new Page<>(pageNo, pageSize), queryWrapper);
        IPage<CountryPageVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<CountryPageVO> records = new ArrayList<>();
        for (WdiCountry country : page.getRecords()) {
            records.add(convertCountryPageVO(country));
        }
        result.setRecords(records);
        return result;
    }

    /**
     * 批量获取国家信息(查询面板展示使用)
     * 查询流程: 参数校验 -> 逐国家读meta缓存 -> 未命中的收集后一次in查询 -> 回写缓存 -> 按入参顺序输出
     */
    @Override
    public List<CountryPageVO> queryCountryBatch(CountryBatchDTO dto) {
        // 1. 参数校验: ID列表为空返回参数错误
        if (CollUtil.isEmpty(dto.getCountryCodeList())) {
            throw new JeecgBootException("国家编码列表不能为空");
        }
        // 2. ID转Long并校验格式
        List<Long> idList = new ArrayList<>();
        for (String countryId : dto.getCountryCodeList()) {
            idList.add(checkAndParseCountryId(countryId));
        }
        // 3. 逐国家读缓存, 未命中的收集待查
        List<CountryPageVO> resultList = new ArrayList<>(idList.size());
        Map<Long, CountryPageVO> voMap = new HashMap<>();
        List<Long> missIds = new ArrayList<>();
        for (Long id : idList) {
            if (voMap.containsKey(id)) {
                continue;
            }
            Object cache = redisUtil.get(COUNTRY_META_CACHE_KEY + id);
            if (cache != null) {
                voMap.put(id, JSON.parseObject(cache.toString(), CountryPageVO.class));
            } else {
                missIds.add(id);
            }
        }
        // 4. 未命中的国家一次in查询后逐个回写缓存
        if (!missIds.isEmpty()) {
            QueryWrapper<WdiCountry> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id", "country_code", "short_name", "country_name_cn", "region", "region_cn", "income_group", "income_group_cn")
                    .in("id", missIds);
            for (WdiCountry country : wdiCountryService.list(queryWrapper)) {
                CountryPageVO vo = convertCountryPageVO(country);
                voMap.put(country.getId(), vo);
                redisUtil.set(COUNTRY_META_CACHE_KEY + country.getId(), JSON.toJSONString(vo), CACHE_SECONDS);
            }
        }
        // 5. 按入参顺序组装输出, 库中不存在的国家跳过
        for (Long id : idList) {
            CountryPageVO vo = voMap.get(id);
            if (vo != null) {
                resultList.add(vo);
            }
        }
        return resultList;
    }

    /**
     * 参数校验: 国家ID为空抛参数错误, 非数字抛格式错误
     *
     * @param countryId 国家ID字符串
     * @return Long类型的国家ID
     */
    private Long checkAndParseCountryId(String countryId) {
        if (StringUtils.isBlank(countryId)) {
            throw new JeecgBootException("国家ID不能为空");
        }
        try {
            return Long.valueOf(countryId.trim());
        } catch (NumberFormatException e) {
            throw new JeecgBootException("国家ID格式错误: " + countryId);
        }
    }

    /**
     * WdiCountry实体转国家信息VO
     */
    private CountryPageVO convertCountryPageVO(WdiCountry country) {
        CountryPageVO vo = new CountryPageVO();
        vo.setCountryCode(country.getCountryCode());
        vo.setShortName(country.getShortName());
        vo.setCountryNameCn(country.getCountryNameCn());
        vo.setRegion(country.getRegion());
        vo.setRegionCn(country.getRegionCn());
        vo.setIncomeGroup(country.getIncomeGroup());
        vo.setIncomeGroupCn(country.getIncomeGroupCn());
        return vo;
    }

    /**
     * 面板数据查询(t_wdi_data约900w行, 重点大表性能防护)
     * 查询流程: 参数强校验 -> 入参清洗 -> 小表t_wdi_country_series预判有效组合 -> 大表索引查询(LIMIT行数防护)
     * -> 名称回填 -> 按viewType组装对应视图(TABLE_LONG长表/TABLE_WIDE宽表透视/CHART图表) -> 可选脚注查询
     */
    @Override
    public WdiDataQueryVO queryPanelData(WdiDataQueryDTO dto) {
        long beginTime = System.currentTimeMillis();
        // ==================== 1. 参数强校验 ====================
        String viewType = validateAndNormalize(dto);

        // ==================== 2. 入参清洗: trim+去重(保持顺序) ====================
        Set<String> seriesCodeSet = dedupTrim(dto.getSeriesCodeList(), "指标编码");
        Set<String> countryCodeSet = dedupTrim(dto.getCountryCodeList(), "国家编码");

        // ==================== 3. 查询前预判: 以t_wdi_data实际数据过滤无效【指标-国家】组合 ====================
        // 预判意义: 全部组合无效时直接短路返回, 避免无效大表扫描; 组合查询走联合索引, 去重组合上限500个
        Map<String, Set<String>> validCountryMap = queryValidComboMap(seriesCodeSet, countryCodeSet);
        Set<String> validSeriesCodes = new LinkedHashSet<>(validCountryMap.keySet());
        Set<String> validCountryCodes = new LinkedHashSet<>();
        int validComboCount = 0;
        for (Map.Entry<String, Set<String>> entry : validCountryMap.entrySet()) {
            validCountryCodes.addAll(entry.getValue());
            validComboCount += entry.getValue().size();
        }
        log.info("WDI面板数据查询: 指标数={}, 国家数={}, 年份范围=[{},{}], viewType={}, 有效指标-国家组合={}个(剔除无效{}个)",
                validSeriesCodes.size(), validCountryCodes.size(), dto.getStartYear(), dto.getEndYear(),
                viewType, validComboCount, seriesCodeSet.size() * countryCodeSet.size() - validComboCount);

        WdiDataQueryVO vo = new WdiDataQueryVO();
        vo.setViewType(viewType);
        vo.setTruncated(false);
        vo.setReturnedRows(0);
        // 全部组合无效: 短路返回空结果, 不查询大表
        if (validSeriesCodes.isEmpty() || validCountryCodes.isEmpty()) {
            log.info("WDI面板数据查询: 无有效指标-国家组合, 短路返回空结果");
            return vo;
        }

        // ==================== 4. 查询面板数据(走联合索引, SQL层LIMIT行数防护) ====================
        // LIMIT MAX_RETURN_ROWS+1: 多查1条用于判断是否被截断
        List<WdiData> dataRows = wdiDataMapper.queryPanelDataRows(validCountryCodes, validSeriesCodes,
                dto.getStartYear(), dto.getEndYear(), MAX_RETURN_ROWS + 1);
        boolean truncated = dataRows.size() > MAX_RETURN_ROWS;
        if (truncated) {
            dataRows = dataRows.subList(0, MAX_RETURN_ROWS);
        }
        vo.setReturnedRows(dataRows.size());
        vo.setTruncated(truncated);
        if (truncated) {
            log.warn("WDI面板数据查询: 结果达到上限{}行被截断, 建议缩小查询范围或使用异步导出", MAX_RETURN_ROWS);
        }

        // ==================== 5. 名称回填: 小表查询国家名/指标名(中文名优先) ====================
        Map<String, String> countryNameMap = queryCountryNameMap(validCountryCodes);
        Map<String, String> seriesNameMap = querySeriesNameMap(validSeriesCodes);

        // ==================== 6. 按viewType组装对应视图 ====================
        switch (viewType) {
            case VIEW_TYPE_TABLE_WIDE:
                // 宽表透视: Java内存行转列(禁止动态SQL行转列)
                vo.setWide(buildWideTable(dataRows, countryNameMap, seriesNameMap));
                break;
            case VIEW_TYPE_CHART:
                // 图表数据: ECharts结构化数据(折线/柱状图)
                vo.setChart(buildChartData(dataRows, countryNameMap, seriesNameMap));
                break;
            case VIEW_TYPE_TABLE_LONG:
            default:
                // 长表: SQL结果直接映射(已按国家,指标,年份排序)
                vo.setLongRows(buildLongRows(dataRows, countryNameMap, seriesNameMap));
                break;
        }

        // ==================== 7. 脚注查询(includeFootnote=true时关联t_wdi_footnote带出) ====================
        // 脚注年份严格按查询年份区间过滤, 避免返回区间外年份的脚注
        if (Boolean.TRUE.equals(dto.getIncludeFootnote())) {
            List<WdiFootnote> footnotes = wdiDataMapper.queryFootnotes(validCountryCodes, validSeriesCodes,
                    dto.getStartYear(), dto.getEndYear(), MAX_FOOTNOTE_ROWS);
            List<WdiDataFootnoteVO> footnoteVos = new ArrayList<>(footnotes.size());
            for (WdiFootnote footnote : footnotes) {
                WdiDataFootnoteVO footnoteVo = new WdiDataFootnoteVO();
                footnoteVo.setCountryCode(footnote.getCountryCode());
                footnoteVo.setCountryName(countryNameMap.get(footnote.getCountryCode()));
                footnoteVo.setSeriesCode(footnote.getSeriesCode());
                footnoteVo.setIndicatorName(seriesNameMap.get(footnote.getSeriesCode()));
                footnoteVo.setYear(footnote.getYear());
                footnoteVo.setFootnoteText(footnote.getFootnoteText());
                footnoteVo.setFootnoteTextCn(footnote.getFootnoteTextCn());
                footnoteVos.add(footnoteVo);
            }
            vo.setFootnotes(footnoteVos);
        }

        // ==================== 8. 指标年份覆盖: t_wdi_series_time聚合各指标年份跨度与覆盖说明 ====================
        vo.setSeriesTimeList(buildSeriesTimeList(validSeriesCodes, seriesNameMap));

        log.info("WDI面板数据查询完成: viewType={}, 命中数据行={}(截断={}), 脚注数={}, 耗时={}ms",
                viewType, dataRows.size(), truncated, vo.getFootnotes() == null ? 0 : vo.getFootnotes().size(),
                System.currentTimeMillis() - beginTime);
        return vo;
    }

    /**
     * 组装指标年份覆盖列表: t_wdi_series_time按指标聚合MIN/MAX年份与覆盖说明
     * t_wdi_series_time为小表(指标×年份行), 一次in+group by查询, 无性能风险
     *
     * @param seriesCodes 指标编码集合(本次查询有效指标)
     * @param seriesNameMap 指标名称映射(中文优先)
     * @return 各指标年份覆盖信息(含年份跨度与覆盖说明)
     */
    private List<WdiSeriesTimeVO> buildSeriesTimeList(Set<String> seriesCodes, Map<String, String> seriesNameMap) {
        QueryWrapper<WdiSeriesTime> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("series_code AS seriesCode", "MIN(year) AS minYear", "MAX(year) AS maxYear",
                        "MAX(description) AS description", "MAX(description_cn) AS descriptionCn")
                .in("series_code", seriesCodes)
                .groupBy("series_code");
        List<WdiSeriesTimeVO> list = new ArrayList<>();
        for (Map<String, Object> row : wdiSeriesTimeService.listMaps(queryWrapper)) {
            WdiSeriesTimeVO item = new WdiSeriesTimeVO();
            item.setIndicatorCode((String) row.get("seriesCode"));
            item.setIndicatorName(seriesNameMap.get(row.get("seriesCode")));
            Object min = row.get("minYear");
            Object max = row.get("maxYear");
            if (min != null) {
                item.setMinYear(Integer.valueOf(min.toString()));
            }
            if (max != null) {
                item.setMaxYear(Integer.valueOf(max.toString()));
            }
            item.setDescription((String) row.get("description"));
            item.setDescriptionCn((String) row.get("descriptionCn"));
            list.add(item);
        }
        return list;
    }

    /**
     * 面板查询参数强校验: 列表非空且不超上限、元素非空、年份区间合法、viewType合法
     * 使用hutool Assert断言, 条件不满足抛JeecgBootException, 由全局异常处理器统一转Result.error返回
     *
     * @param dto 查询入参
     * @return 规范化后的viewType(空默认TABLE_LONG)
     */
    private String validateAndNormalize(WdiDataQueryDTO dto) {
        if (dto == null) {
            throw new JeecgBootException("查询参数不能为空");
        }
        if (dto.getSeriesCodeList() == null || dto.getSeriesCodeList().isEmpty()) {
            throw new JeecgBootException("指标编码列表不能为空");
        }
        if (dto.getSeriesCodeList().size() > MAX_SERIES_COUNT) {
            throw new JeecgBootException("选中指标过多(最多" + MAX_SERIES_COUNT + "个), 请减少指标数量或使用数据导出功能");
        }
        if (dto.getCountryCodeList() == null || dto.getCountryCodeList().isEmpty()) {
            throw new JeecgBootException("国家编码列表不能为空");
        }
        if (dto.getCountryCodeList().size() > MAX_COUNTRY_COUNT) {
            throw new JeecgBootException("选中国家过多(最多" + MAX_COUNTRY_COUNT + "个), 请减少国家数量或使用数据导出功能");
        }
        if (dto.getStartYear() != null && dto.getEndYear() != null && dto.getStartYear() > dto.getEndYear()) {
            throw new JeecgBootException("起始年份不能大于结束年份");
        }
        String viewType = StringUtils.defaultIfBlank(dto.getViewType(), VIEW_TYPE_TABLE_LONG).trim().toUpperCase();
        if (!VIEW_TYPE_TABLE_LONG.equals(viewType) && !VIEW_TYPE_TABLE_WIDE.equals(viewType) && !VIEW_TYPE_CHART.equals(viewType)) {
            throw new JeecgBootException("viewType不合法, 仅支持: TABLE_LONG/TABLE_WIDE/CHART");
        }
        return viewType;
    }

    /**
     * 面板查询编码列表清洗: trim+去重(保持顺序), 空白元素直接拒绝
     *
     * @param codeList 编码列表
     * @param name     元素名称(用于异常提示)
     * @return 清洗后的编码集合
     */
    private Set<String> dedupTrim(List<String> codeList, String name) {
        Set<String> codeSet = new LinkedHashSet<>();
        for (String code : codeList) {
            Assert.isTrue(StringUtils.isNotBlank(code), () -> new JeecgBootException(name + "不能为空"));
            codeSet.add(code.trim());
        }
        return codeSet;
    }

    /**
     * 查询有效【指标-国家】组合: 直接查t_wdi_data实际数据取去重组合
     * 说明: t_wdi_country_series与t_wdi_data实际数据存在错位, 不能作为有效性判断依据;
     * 查询走(series_code, country_code)联合索引, 指标数<=10且国家数<=50, 去重组合上限500个, 开销可控
     *
     * @param seriesCodeSet 指标编码集合
     * @param countryCodeSet 国家编码集合
     * @return key=指标编码, value=该指标在t_wdi_data中有实际数据的国家编码集合
     */
    private Map<String, Set<String>> queryValidComboMap(Set<String> seriesCodeSet, Set<String> countryCodeSet) {
        QueryWrapper<WdiData> queryWrapper = new QueryWrapper<>();
        // t_wdi_data的指标列名为indicator_code, 存储的值即t_wdi_series.series_code
        queryWrapper.select("DISTINCT indicator_code", "country_code")
                .in("indicator_code", seriesCodeSet)
                .in("country_code", countryCodeSet);
        Map<String, Set<String>> validMap = new HashMap<>();
        for (WdiData data : wdiDataMapper.selectList(queryWrapper)) {
            if (StringUtils.isNotBlank(data.getCountryCode())) {
                validMap.computeIfAbsent(data.getIndicatorCode(), key -> new HashSet<>())
                        .add(data.getCountryCode());
            }
        }
        return validMap;
    }

    /**
     * 查询国家名称映射(中文名优先, 无中文名回退英文简称)
     *
     * @param countryCodes 国家编码集合
     * @return key=国家编码, value=展示名称
     */
    private Map<String, String> queryCountryNameMap(Set<String> countryCodes) {
        QueryWrapper<WdiCountry> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("country_code", "short_name", "country_name_cn")
                .in("country_code", countryCodes);
        Map<String, String> nameMap = new HashMap<>();
        for (WdiCountry country : wdiCountryService.list(queryWrapper)) {
            nameMap.put(country.getCountryCode(),
                    StringUtils.defaultIfBlank(country.getCountryNameCn(), country.getShortName()));
        }
        return nameMap;
    }

    /**
     * 查询指标名称映射(中文名优先, 无中文名回退英文名)
     *
     * @param seriesCodes 指标编码集合
     * @return key=指标编码, value=展示名称
     */
    private Map<String, String> querySeriesNameMap(Set<String> seriesCodes) {
        QueryWrapper<WdiSeries> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("series_code", "indicator_name", "indicator_name_cn")
                .in("series_code", seriesCodes);
        Map<String, String> nameMap = new HashMap<>();
        for (WdiSeries series : wdiSeriesService.list(queryWrapper)) {
            nameMap.put(series.getSeriesCode(),
                    StringUtils.defaultIfBlank(series.getIndicatorNameCn(), series.getIndicatorName()));
        }
        return nameMap;
    }

    /**
     * 组装长表视图(TABLE_LONG): 国家,指标,年份,数值
     *
     * @param dataRows       底层数据行(已按国家,指标,年份排序)
     * @param countryNameMap 国家名称映射
     * @param seriesNameMap  指标名称映射
     * @return 长表行列表
     */
    private List<WdiDataLongRowVO> buildLongRows(List<WdiData> dataRows, Map<String, String> countryNameMap, Map<String, String> seriesNameMap) {
        List<WdiDataLongRowVO> longRows = new ArrayList<>(dataRows.size());
        for (WdiData data : dataRows) {
            WdiDataLongRowVO row = new WdiDataLongRowVO();
            row.setCountryCode(data.getCountryCode());
            row.setCountryName(countryNameMap.get(data.getCountryCode()));
            row.setSeriesCode(data.getIndicatorCode());
            row.setIndicatorName(seriesNameMap.get(data.getIndicatorCode()));
            row.setYear(data.getYear());
            row.setValue(data.getValue());
            longRows.add(row);
        }
        return longRows;
    }

    /**
     * 组装宽表视图(TABLE_WIDE): Java内存行转列透视(禁止动态SQL行转列)
     * 行=国家×指标组合(单指标时即国家行), 列=年份(升序), 单元格=指标值
     *
     * @param dataRows       底层数据行(长表)
     * @param countryNameMap 国家名称映射
     * @param seriesNameMap  指标名称映射
     * @return 宽表结构
     */
    private WdiDataWideVO buildWideTable(List<WdiData> dataRows, Map<String, String> countryNameMap, Map<String, String> seriesNameMap) {
        // 1. 列 = 数据中出现过的年份并集(升序)
        TreeSet<Integer> yearSet = new TreeSet<>();
        for (WdiData data : dataRows) {
            yearSet.add(data.getYear());
        }
        List<Integer> yearColumns = new ArrayList<>(yearSet);
        // 2. 按国家×指标分组(LinkedHashMap保持数据出现顺序), 收集各行(指标,年份)->值
        Map<String, WdiDataWideVO.WideRow> rowMap = new LinkedHashMap<>();
        Map<String, Map<Integer, BigDecimal>> valueMap = new HashMap<>();
        for (WdiData data : dataRows) {
            String groupKey = data.getCountryCode() + GROUP_KEY_SEPARATOR + data.getIndicatorCode();
            rowMap.computeIfAbsent(groupKey, key -> {
                WdiDataWideVO.WideRow row = new WdiDataWideVO.WideRow();
                row.setCountryCode(data.getCountryCode());
                row.setCountryName(countryNameMap.get(data.getCountryCode()));
                row.setSeriesCode(data.getIndicatorCode());
                row.setIndicatorName(seriesNameMap.get(data.getIndicatorCode()));
                return row;
            });
            valueMap.computeIfAbsent(groupKey, key -> new HashMap<>()).put(data.getYear(), data.getValue());
        }
        // 3. 按年份列对齐输出单元格值(缺失年份为null)
        List<WdiDataWideVO.WideRow> rows = new ArrayList<>(rowMap.size());
        for (Map.Entry<String, WdiDataWideVO.WideRow> entry : rowMap.entrySet()) {
            Map<Integer, BigDecimal> yearValueMap = valueMap.get(entry.getKey());
            List<BigDecimal> values = new ArrayList<>(yearColumns.size());
            for (Integer year : yearColumns) {
                values.add(yearValueMap.get(year));
            }
            entry.getValue().setValues(values);
            rows.add(entry.getValue());
        }
        // 4. 组装宽表
        WdiDataWideVO wide = new WdiDataWideVO();
        wide.setYearColumns(yearColumns);
        wide.setRows(rows);
        return wide;
    }

    /**
     * 组装图表视图(CHART): ECharts折线/柱状图可直接使用的结构化数据
     * xAxis.data=years(升序), 每条系列=一个国家×指标组合, 缺失年份为null(ECharts自动断点)
     *
     * @param dataRows       底层数据行(长表)
     * @param countryNameMap 国家名称映射
     * @param seriesNameMap  指标名称映射
     * @return 图表结构
     */
    private WdiDataChartVO buildChartData(List<WdiData> dataRows, Map<String, String> countryNameMap, Map<String, String> seriesNameMap) {
        // 1. X轴 = 数据中出现过的年份并集(升序)
        TreeSet<Integer> yearSet = new TreeSet<>();
        for (WdiData data : dataRows) {
            yearSet.add(data.getYear());
        }
        List<Integer> years = new ArrayList<>(yearSet);
        // 2. 按国家×指标分组构建系列(LinkedHashMap保持数据出现顺序)
        Map<String, WdiDataChartVO.ChartSeries> seriesMap = new LinkedHashMap<>();
        Map<String, Map<Integer, BigDecimal>> valueMap = new HashMap<>();
        for (WdiData data : dataRows) {
            String groupKey = data.getCountryCode() + GROUP_KEY_SEPARATOR + data.getIndicatorCode();
            seriesMap.computeIfAbsent(groupKey, key -> {
                WdiDataChartVO.ChartSeries chartSeries = new WdiDataChartVO.ChartSeries();
                String countryName = countryNameMap.get(data.getCountryCode());
                String indicatorName = seriesNameMap.get(data.getIndicatorCode());
                chartSeries.setCountryCode(data.getCountryCode());
                chartSeries.setCountryName(countryName);
                chartSeries.setSeriesCode(data.getIndicatorCode());
                chartSeries.setIndicatorName(indicatorName);
                chartSeries.setName(StringUtils.defaultIfBlank(indicatorName, data.getIndicatorCode())
                        + "-" + StringUtils.defaultIfBlank(countryName, data.getCountryCode()));
                return chartSeries;
            });
            valueMap.computeIfAbsent(groupKey, key -> new HashMap<>()).put(data.getYear(), data.getValue());
        }
        // 3. 按X轴年份对齐系列值(缺失年份为null)
        for (Map.Entry<String, WdiDataChartVO.ChartSeries> entry : seriesMap.entrySet()) {
            Map<Integer, BigDecimal> yearValueMap = valueMap.get(entry.getKey());
            List<BigDecimal> seriesData = new ArrayList<>(years.size());
            for (Integer year : years) {
                seriesData.add(yearValueMap.get(year));
            }
            entry.getValue().setData(seriesData);
        }
        // 4. 组装图表结构
        WdiDataChartVO chart = new WdiDataChartVO();
        chart.setYears(years);
        chart.setSeriesList(new ArrayList<>(seriesMap.values()));
        return chart;
    }

    /**
     * WDI统计概览
     * 查询流程: 优先读Redis缓存(应用启动时由WdiStatisticsComponent预加载) -> 缓存未命中时委托统计组件回源查库并回写缓存
     * 回源查询逻辑(CompletableFuture多线程并行查库)收敛在WdiStatisticsComponent中
     */
    @Override
    public WdiStatisticsVO getStatisticsSummary() {
        // 1. 优先读取Redis缓存(缓存值为JSON字符串, 规避反序列化类型问题)
        Object cache = redisUtil.get(WdiStatisticsComponent.STATISTICS_CACHE_KEY);
        if (cache != null) {
            return JSON.parseObject(cache.toString(), WdiStatisticsVO.class);
        }
        // 2. 缓存未命中: 委托统计组件回源查库并回写缓存
        return wdiStatisticsComponent.refreshStatisticsCache();
    }
}
