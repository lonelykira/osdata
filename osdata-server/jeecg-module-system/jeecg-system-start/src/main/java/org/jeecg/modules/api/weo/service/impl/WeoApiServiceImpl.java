package org.jeecg.modules.api.weo.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.api.component.WeoStatisticsComponent;
import org.jeecg.modules.api.weo.dto.CountryBatchDTO;
import org.jeecg.modules.api.weo.dto.CountryPageDTO;
import org.jeecg.modules.api.weo.dto.IndicatorBatchDTO;
import org.jeecg.modules.api.weo.dto.IndicatorPageDTO;
import org.jeecg.modules.api.weo.dto.IndicatorYearRangeDTO;
import org.jeecg.modules.api.weo.dto.TopicPageDTO;
import org.jeecg.modules.api.weo.dto.WeoDataQueryDTO;
import org.jeecg.modules.api.weo.service.IWeoApiService;
import org.jeecg.modules.api.weo.vo.CountryPageVO;
import org.jeecg.modules.api.weo.vo.CountryTreeVO;
import org.jeecg.modules.api.weo.vo.IndicatorDetailVO;
import org.jeecg.modules.api.weo.vo.IndicatorPageVO;
import org.jeecg.modules.api.weo.vo.IndicatorYearRangeVO;
import org.jeecg.modules.api.weo.vo.TopicPageVO;
import org.jeecg.modules.api.weo.vo.TopicTreeVO;
import org.jeecg.modules.api.weo.vo.WeoDataChartVO;
import org.jeecg.modules.api.weo.vo.WeoDataLongRowVO;
import org.jeecg.modules.api.weo.vo.WeoDataQueryVO;
import org.jeecg.modules.api.weo.vo.WeoDataTimeSeriesVO;
import org.jeecg.modules.api.weo.vo.WeoDataWideVO;
import org.jeecg.modules.api.weo.vo.WeoStatisticsVO;
import org.jeecg.modules.weo.entity.WeoCountry;
import org.jeecg.modules.weo.entity.WeoData;
import org.jeecg.modules.weo.entity.WeoIndicator;
import org.jeecg.modules.weo.entity.WeoTopic;
import org.jeecg.modules.weo.mapper.WeoDataMapper;
import org.jeecg.modules.weo.service.IWeoCountryService;
import org.jeecg.modules.weo.service.IWeoIndicatorService;
import org.jeecg.modules.weo.service.IWeoTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: WEO数据模块webapi服务实现
 * 元数据接口(主题/指标/国家)统一Redis缓存1小时(缓存key前缀weo:), 统计概览缓存7天
 * 主题与指标的关联关系: t_weo_indicator.topic_code = t_weo_topic.topic_en
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class WeoApiServiceImpl implements IWeoApiService {

    /**主题树缓存key*/
    private static final String TOPIC_TREE_CACHE_KEY = "weo:topic:tree";
    /**指标详情缓存key前缀*/
    private static final String INDICATOR_DETAIL_CACHE_KEY = "weo:indicator:detail:";
    /**指标元信息缓存key前缀*/
    private static final String INDICATOR_META_CACHE_KEY = "weo:indicator:meta:";
    /**指标可用国家清单缓存key前缀*/
    private static final String INDICATOR_COUNTRY_CACHE_KEY = "weo:indicator:country:";
    /**国家分组树缓存key*/
    private static final String COUNTRY_TREE_CACHE_KEY = "weo:country:tree";
    /**国家元信息缓存key前缀*/
    private static final String COUNTRY_META_CACHE_KEY = "weo:country:meta:";
    /**元数据缓存过期时间: 1小时*/
    private static final long CACHE_SECONDS = 3600L;
    /**分页每页条数上限*/
    private static final int MAX_PAGE_SIZE = 100;
    /**视图类型: 长表*/
    public static final String VIEW_TYPE_TABLE_LONG = "TABLE_LONG";
    /**视图类型: 宽表透视*/
    public static final String VIEW_TYPE_TABLE_WIDE = "TABLE_WIDE";
    /**视图类型: 指标时序(每国家一行, 一级表头=指标, 二级表头=年份)*/
    public static final String VIEW_TYPE_TIME_SERIES = "TIME_SERIES";
    /**视图类型: 图表*/
    public static final String VIEW_TYPE_CHART = "CHART";
    /**面板查询单次指标数量上限*/
    private static final int MAX_INDICATOR_COUNT = 10;
    /**面板查询单次国家数量上限*/
    private static final int MAX_COUNTRY_COUNT = 10;
    /**面板查询返回行数上限*/
    private static final int MAX_RETURN_ROWS = 50000;
    /**宽表/图表分组key分隔符(国家编码与指标编码拼接)*/
    private static final String GROUP_KEY_SEPARATOR = "||";
    /**国家分组树: 单个国家分组编码*/
    private static final String GROUP_COUNTRY = "COUNTRY";
    /**国家分组树: 聚合经济体分组编码*/
    private static final String GROUP_AGGREGATE = "AGGREGATE";

    @Autowired
    private IWeoTopicService weoTopicService;
    @Autowired
    private IWeoIndicatorService weoIndicatorService;
    @Autowired
    private IWeoCountryService weoCountryService;
    @Autowired
    private WeoDataMapper weoDataMapper;
    @Autowired
    private WeoStatisticsComponent weoStatisticsComponent;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<TopicTreeVO> getTopicTree() {
        Object cache = redisUtil.get(TOPIC_TREE_CACHE_KEY);
        if (cache != null) {
            return JSON.parseArray(cache.toString(), TopicTreeVO.class);
        }
        // 主题全量小表, 按前端展示排序号排序
        QueryWrapper<WeoTopic> topicWrapper = new QueryWrapper<>();
        topicWrapper.orderByAsc("sort_order", "topic_en");
        List<WeoTopic> topics = weoTopicService.list(topicWrapper);
        // 指标全量小表, 按主题编码分组(指标量级百级, 一次查询内存组装, 禁止N+1)
        QueryWrapper<WeoIndicator> indicatorWrapper = new QueryWrapper<>();
        indicatorWrapper.orderByAsc("indicator_code");
        Map<String, List<TopicTreeVO.IndicatorNode>> indicatorGroupMap = new HashMap<>();
        for (WeoIndicator indicator : weoIndicatorService.list(indicatorWrapper)) {
            if (StringUtils.isBlank(indicator.getTopicCode())) {
                continue;
            }
            TopicTreeVO.IndicatorNode node = new TopicTreeVO.IndicatorNode();
            node.setIndicatorCode(indicator.getIndicatorCode());
            node.setIndicatorName(indicator.getIndicatorEn());
            node.setIndicatorNameCn(indicator.getIndicatorCn());
            indicatorGroupMap.computeIfAbsent(indicator.getTopicCode(), key -> new ArrayList<>()).add(node);
        }
        // 一级节点=主题(关联字段: indicator.topicCode = topic.topicEn), 子节点=该主题下的指标
        List<TopicTreeVO> tree = new ArrayList<>(topics.size());
        for (WeoTopic topic : topics) {
            TopicTreeVO node = new TopicTreeVO();
            node.setTopicCode(topic.getTopicEn());
            node.setTopicEn(topic.getTopicEn());
            node.setTopicCn(topic.getTopicCn());
            node.setChildren(indicatorGroupMap.getOrDefault(topic.getTopicEn(), new ArrayList<>()));
            tree.add(node);
        }
        redisUtil.set(TOPIC_TREE_CACHE_KEY, JSON.toJSONString(tree), CACHE_SECONDS);
        return tree;
    }

    @Override
    public IPage<TopicPageVO> queryTopicPage(TopicPageDTO dto) {
        int pageNo = normalizePageNo(dto == null ? null : dto.getPageNo());
        int pageSize = normalizePageSize(dto == null ? null : dto.getPageSize());
        // 主题分页(按展示排序号), 指标计数一次group by查询后内存组装, 禁止逐行count
        QueryWrapper<WeoTopic> topicWrapper = new QueryWrapper<>();
        topicWrapper.orderByAsc("sort_order", "topic_en");
        IPage<WeoTopic> page = weoTopicService.page(new Page<>(pageNo, pageSize), topicWrapper);
        QueryWrapper<WeoIndicator> countWrapper = new QueryWrapper<>();
        countWrapper.select("topic_code AS topicCode", "COUNT(1) AS cnt").groupBy("topic_code");
        Map<String, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : safeListMaps(weoIndicatorService.listMaps(countWrapper))) {
            Object topicCode = row.get("topicCode");
            Object cnt = row.get("cnt");
            if (topicCode != null && cnt != null) {
                countMap.put(topicCode.toString(), Long.valueOf(cnt.toString()));
            }
        }
        List<TopicPageVO> records = new ArrayList<>(page.getRecords().size());
        for (WeoTopic topic : page.getRecords()) {
            TopicPageVO vo = new TopicPageVO();
            vo.setTopicCode(topic.getTopicEn());
            vo.setTopicEn(topic.getTopicEn());
            vo.setTopicCn(topic.getTopicCn());
            vo.setIndicatorCount(countMap.getOrDefault(topic.getTopicEn(), 0L));
            records.add(vo);
        }
        Page<TopicPageVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public IPage<IndicatorPageVO> queryIndicatorPage(IndicatorPageDTO dto) {
        int pageNo = normalizePageNo(dto == null ? null : dto.getPageNo());
        int pageSize = normalizePageSize(dto == null ? null : dto.getPageSize());
        String topicCode = dto == null ? null : StringUtils.trimToNull(dto.getTopicCode());
        String keyword = dto == null ? null : StringUtils.trimToNull(dto.getKeyword());
        QueryWrapper<WeoIndicator> wrapper = new QueryWrapper<>();
        if (topicCode != null) {
            wrapper.eq("topic_code", topicCode);
        }
        if (keyword != null) {
            wrapper.and(w -> w.like("indicator_code", keyword)
                    .or().like("indicator_en", keyword)
                    .or().like("indicator_cn", keyword));
        }
        wrapper.orderByAsc("indicator_code");
        IPage<WeoIndicator> page = weoIndicatorService.page(new Page<>(pageNo, pageSize), wrapper);
        // 主题中文名一次全量查询内存映射(主题小表)
        Map<String, String> topicCnMap = queryTopicCnMap();
        List<IndicatorPageVO> records = new ArrayList<>(page.getRecords().size());
        for (WeoIndicator indicator : page.getRecords()) {
            records.add(buildIndicatorPageVO(indicator, topicCnMap));
        }
        Page<IndicatorPageVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public IndicatorDetailVO getIndicatorDetail(String indicatorCode) {
        Assert.isTrue(StringUtils.isNotBlank(indicatorCode), () -> new JeecgBootException("指标编码不能为空"));
        String cacheKey = INDICATOR_DETAIL_CACHE_KEY + indicatorCode.trim();
        Object cache = redisUtil.get(cacheKey);
        if (cache != null) {
            return JSON.parseObject(cache.toString(), IndicatorDetailVO.class);
        }
        WeoIndicator indicator = weoIndicatorService.findByIndicatorCode(indicatorCode.trim());
        if (indicator == null) {
            throw new JeecgBootException("未找到对应指标");
        }
        IndicatorDetailVO vo = new IndicatorDetailVO();
        vo.setId(indicator.getId());
        vo.setIndicatorCode(indicator.getIndicatorCode());
        vo.setIndicatorName(indicator.getIndicatorEn());
        vo.setIndicatorNameCn(indicator.getIndicatorCn());
        vo.setTopicCode(indicator.getTopicCode());
        vo.setTopicCn(queryTopicCnMap().get(indicator.getTopicCode()));
        vo.setUnit(indicator.getUnit());
        vo.setScale(indicator.getScale());
        redisUtil.set(cacheKey, JSON.toJSONString(vo), CACHE_SECONDS);
        return vo;
    }

    @Override
    public List<IndicatorPageVO> queryIndicatorBatch(IndicatorBatchDTO dto) {
        if (dto == null || dto.getIndicatorCodeList() == null || dto.getIndicatorCodeList().isEmpty()) {
            throw new JeecgBootException("指标编码列表不能为空");
        }
        Set<String> codeSet = dedupTrim(dto.getIndicatorCodeList(), "指标编码");
        // 编码->元信息: 逐个读缓存, 未命中的编码一次IN查询后回填缓存
        Map<String, IndicatorPageVO> voMap = new LinkedHashMap<>();
        List<String> missCodes = new ArrayList<>();
        for (String code : codeSet) {
            Object cache = redisUtil.get(INDICATOR_META_CACHE_KEY + code);
            if (cache != null) {
                voMap.put(code, JSON.parseObject(cache.toString(), IndicatorPageVO.class));
            } else {
                missCodes.add(code);
            }
        }
        if (!missCodes.isEmpty()) {
            Map<String, String> topicCnMap = queryTopicCnMap();
            QueryWrapper<WeoIndicator> wrapper = new QueryWrapper<>();
            wrapper.in("indicator_code", missCodes);
            for (WeoIndicator indicator : weoIndicatorService.list(wrapper)) {
                IndicatorPageVO vo = buildIndicatorPageVO(indicator, topicCnMap);
                voMap.put(indicator.getIndicatorCode(), vo);
                redisUtil.set(INDICATOR_META_CACHE_KEY + indicator.getIndicatorCode(), JSON.toJSONString(vo), CACHE_SECONDS);
            }
        }
        // 按入参顺序输出, 无效编码位置为null(调用方按需跳过)
        List<IndicatorPageVO> result = new ArrayList<>(codeSet.size());
        for (String code : codeSet) {
            result.add(voMap.get(code));
        }
        return result;
    }

    @Override
    public List<CountryPageVO> queryIndicatorCountry(String indicatorCode) {
        Assert.isTrue(StringUtils.isNotBlank(indicatorCode), () -> new JeecgBootException("指标编码不能为空"));
        String code = indicatorCode.trim();
        String cacheKey = INDICATOR_COUNTRY_CACHE_KEY + code;
        Object cache = redisUtil.get(cacheKey);
        if (cache != null) {
            return JSON.parseArray(cache.toString(), CountryPageVO.class);
        }
        // 该指标在t_weo_data中有实际数据的去重国家编码(走idx_indicator_code索引)
        QueryWrapper<WeoData> dataWrapper = new QueryWrapper<>();
        dataWrapper.select("DISTINCT country_code").eq("indicator_code", code);
        Set<String> countryCodeSet = new LinkedHashSet<>();
        for (Map<String, Object> row : safeListMaps(weoDataMapper.selectMaps(dataWrapper))) {
            Object cc = row.get("country_code");
            if (cc != null && StringUtils.isNotBlank(cc.toString())) {
                countryCodeSet.add(cc.toString());
            }
        }
        List<CountryPageVO> result = new ArrayList<>();
        if (!countryCodeSet.isEmpty()) {
            QueryWrapper<WeoCountry> countryWrapper = new QueryWrapper<>();
            countryWrapper.in("country_code", countryCodeSet).orderByAsc("country_en");
            for (WeoCountry country : weoCountryService.list(countryWrapper)) {
                result.add(buildCountryPageVO(country));
            }
        }
        redisUtil.set(cacheKey, JSON.toJSONString(result), CACHE_SECONDS);
        return result;
    }

    @Override
    public IndicatorYearRangeVO queryIndicatorYearRange(IndicatorYearRangeDTO dto) {
        if (dto == null || dto.getIndicatorCodeList() == null || dto.getIndicatorCodeList().isEmpty()) {
            throw new JeecgBootException("指标编码列表不能为空");
        }
        Set<String> codeSet = dedupTrim(dto.getIndicatorCodeList(), "指标编码");
        // 各指标年份区间一次group by查询(走idx_indicator_code索引), 再内存取交集
        QueryWrapper<WeoData> wrapper = new QueryWrapper<>();
        wrapper.select("indicator_code AS indicatorCode", "MIN(year) AS minYear", "MAX(year) AS maxYear")
                .in("indicator_code", codeSet)
                .groupBy("indicator_code");
        Integer minYear = null;
        Integer maxYear = null;
        for (Map<String, Object> row : safeListMaps(weoDataMapper.selectMaps(wrapper))) {
            Object min = row.get("minYear");
            Object max = row.get("maxYear");
            if (min == null || max == null) {
                continue;
            }
            // 交集: 最小年份取各指标最小年份的最大值, 最大年份取各指标最大年份的最小值
            int itemMin = Integer.parseInt(min.toString());
            int itemMax = Integer.parseInt(max.toString());
            minYear = minYear == null ? itemMin : Math.max(minYear, itemMin);
            maxYear = maxYear == null ? itemMax : Math.min(maxYear, itemMax);
        }
        IndicatorYearRangeVO vo = new IndicatorYearRangeVO();
        vo.setMinYear(minYear);
        vo.setMaxYear(maxYear);
        return vo;
    }

    @Override
    public List<CountryTreeVO> queryCountryTree() {
        Object cache = redisUtil.get(COUNTRY_TREE_CACHE_KEY);
        if (cache != null) {
            return JSON.parseArray(cache.toString(), CountryTreeVO.class);
        }
        // 全量小表一次查询, 按(单个国家/聚合经济体)分组, 组内按中文名排序
        QueryWrapper<WeoCountry> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("country_code");
        Map<Integer, List<CountryTreeVO.CountryNode>> groupMap = new HashMap<>();
        for (WeoCountry country : weoCountryService.list(wrapper)) {
            CountryTreeVO.CountryNode node = new CountryTreeVO.CountryNode();
            node.setId(country.getId());
            node.setCountryCode(country.getCountryCode());
            node.setCountryEn(country.getCountryEn());
            node.setCountryCn(country.getCountryCn());
            node.setIsAggregate(country.getIsAggregate());
            groupMap.computeIfAbsent(country.getIsAggregate() == null ? 0 : country.getIsAggregate(), key -> new ArrayList<>()).add(node);
        }
        List<CountryTreeVO> tree = new ArrayList<>(2);
        if (groupMap.containsKey(0)) {
            tree.add(buildCountryGroup(GROUP_COUNTRY, "Countries", "国家", groupMap.get(0)));
        }
        if (groupMap.containsKey(1)) {
            tree.add(buildCountryGroup(GROUP_AGGREGATE, "Aggregate Economies", "聚合经济体", groupMap.get(1)));
        }
        redisUtil.set(COUNTRY_TREE_CACHE_KEY, JSON.toJSONString(tree), CACHE_SECONDS);
        return tree;
    }

    @Override
    public IPage<CountryPageVO> queryCountryPage(CountryPageDTO dto) {
        int pageNo = normalizePageNo(dto == null ? null : dto.getPageNo());
        int pageSize = normalizePageSize(dto == null ? null : dto.getPageSize());
        String keyword = dto == null ? null : StringUtils.trimToNull(dto.getKeyword());
        Integer isAggregate = dto == null ? null : dto.getIsAggregate();
        QueryWrapper<WeoCountry> wrapper = new QueryWrapper<>();
        if (keyword != null) {
            wrapper.and(w -> w.like("country_code", keyword)
                    .or().like("country_en", keyword)
                    .or().like("country_cn", keyword));
        }
        if (isAggregate != null) {
            wrapper.eq("is_aggregate", isAggregate);
        }
        wrapper.orderByAsc("country_cn", "country_en");
        IPage<WeoCountry> page = weoCountryService.page(new Page<>(pageNo, pageSize), wrapper);
        List<CountryPageVO> records = new ArrayList<>(page.getRecords().size());
        for (WeoCountry country : page.getRecords()) {
            records.add(buildCountryPageVO(country));
        }
        Page<CountryPageVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public List<CountryPageVO> queryCountryBatch(CountryBatchDTO dto) {
        if (dto == null || dto.getCountryCodeList() == null || dto.getCountryCodeList().isEmpty()) {
            throw new JeecgBootException("国家编码列表不能为空");
        }
        Set<String> codeSet = dedupTrim(dto.getCountryCodeList(), "国家编码");
        // 编码->信息: 逐个读缓存, 未命中的编码一次IN查询后回填缓存
        Map<String, CountryPageVO> voMap = new LinkedHashMap<>();
        List<String> missCodes = new ArrayList<>();
        for (String code : codeSet) {
            Object cache = redisUtil.get(COUNTRY_META_CACHE_KEY + code);
            if (cache != null) {
                voMap.put(code, JSON.parseObject(cache.toString(), CountryPageVO.class));
            } else {
                missCodes.add(code);
            }
        }
        if (!missCodes.isEmpty()) {
            QueryWrapper<WeoCountry> wrapper = new QueryWrapper<>();
            wrapper.in("country_code", missCodes);
            for (WeoCountry country : weoCountryService.list(wrapper)) {
                CountryPageVO vo = buildCountryPageVO(country);
                voMap.put(country.getCountryCode(), vo);
                redisUtil.set(COUNTRY_META_CACHE_KEY + country.getCountryCode(), JSON.toJSONString(vo), CACHE_SECONDS);
            }
        }
        // 按入参顺序输出, 无效编码位置为null(调用方按需跳过)
        List<CountryPageVO> result = new ArrayList<>(codeSet.size());
        for (String code : codeSet) {
            result.add(voMap.get(code));
        }
        return result;
    }

    @Override
    public WeoDataQueryVO queryPanelData(WeoDataQueryDTO dto) {
        long beginTime = System.currentTimeMillis();
        // ==================== 1. 参数强校验 ====================
        String viewType = validateAndNormalize(dto);

        // ==================== 2. 入参清洗: trim+去重(保持顺序) ====================
        Set<String> indicatorCodeSet = dedupTrim(dto.getIndicatorCodeList(), "指标编码");
        Set<String> countryCodeSet = dedupTrim(dto.getCountryCodeList(), "国家编码");

        // ==================== 3. 查询前预判: 以t_weo_data实际数据过滤无效【指标-国家】组合 ====================
        // 预判意义: 全部组合无效时直接短路返回, 避免无效大表扫描; 组合查询走联合唯一索引, 去重组合上限100个
        Map<String, Set<String>> validCountryMap = queryValidComboMap(indicatorCodeSet, countryCodeSet);
        Set<String> validIndicatorCodes = new LinkedHashSet<>(validCountryMap.keySet());
        Set<String> validCountryCodes = new LinkedHashSet<>();
        int validComboCount = 0;
        for (Map.Entry<String, Set<String>> entry : validCountryMap.entrySet()) {
            validCountryCodes.addAll(entry.getValue());
            validComboCount += entry.getValue().size();
        }
        log.info("WEO面板数据查询: 指标数={}, 国家数={}, 年份范围=[{},{}], viewType={}, 有效指标-国家组合={}个(剔除无效{}个)",
                validIndicatorCodes.size(), validCountryCodes.size(), dto.getStartYear(), dto.getEndYear(),
                viewType, validComboCount, indicatorCodeSet.size() * countryCodeSet.size() - validComboCount);

        WeoDataQueryVO vo = new WeoDataQueryVO();
        vo.setViewType(viewType);
        vo.setTruncated(false);
        vo.setReturnedRows(0);
        // 全部组合无效: 短路返回空结果, 不查询大表
        if (validIndicatorCodes.isEmpty() || validCountryCodes.isEmpty()) {
            log.info("WEO面板数据查询: 无有效指标-国家组合, 短路返回空结果");
            return vo;
        }

        // ==================== 4. 查询面板数据(走联合唯一索引, SQL层LIMIT行数防护) ====================
        // LIMIT MAX_RETURN_ROWS+1: 多查1条用于判断是否被截断
        List<WeoData> dataRows = weoDataMapper.queryPanelDataRows(validCountryCodes, validIndicatorCodes,
                dto.getStartYear(), dto.getEndYear(), MAX_RETURN_ROWS + 1);
        boolean truncated = dataRows.size() > MAX_RETURN_ROWS;
        if (truncated) {
            dataRows = dataRows.subList(0, MAX_RETURN_ROWS);
        }
        vo.setReturnedRows(dataRows.size());
        vo.setTruncated(truncated);
        if (truncated) {
            log.warn("WEO面板数据查询: 结果达到上限{}行被截断, 建议缩小查询范围或使用异步导出", MAX_RETURN_ROWS);
        }

        // ==================== 5. 名称回填: 小表查询国家名/指标名(中文名优先) ====================
        Map<String, String> countryNameMap = queryCountryNameMap(validCountryCodes);
        Map<String, String> indicatorNameMap = queryIndicatorNameMap(validIndicatorCodes);

        // ==================== 6. 按viewType组装对应视图 ====================
        switch (viewType) {
            case VIEW_TYPE_TABLE_WIDE:
                // 宽表透视: Java内存行转列(禁止动态SQL行转列)
                vo.setWide(buildWideTable(dataRows, countryNameMap, indicatorNameMap));
                break;
            case VIEW_TYPE_TIME_SERIES:
                // 指标时序: 每个国家一行, 一级表头=指标, 二级表头=年份(Java内存透视)
                vo.setTimeSeries(buildTimeSeriesTable(dataRows, countryNameMap, indicatorNameMap, validIndicatorCodes));
                break;
            case VIEW_TYPE_CHART:
                // 图表数据: ECharts结构化数据(折线/柱状图)
                vo.setChart(buildChartData(dataRows, countryNameMap, indicatorNameMap));
                break;
            case VIEW_TYPE_TABLE_LONG:
            default:
                // 长表: SQL结果直接映射(已按国家,指标,年份排序)
                vo.setLongRows(buildLongRows(dataRows, countryNameMap, indicatorNameMap));
                break;
        }

        log.info("WEO面板数据查询完成: viewType={}, 命中数据行={}(截断={}), 耗时={}ms",
                viewType, dataRows.size(), truncated, System.currentTimeMillis() - beginTime);
        return vo;
    }

    @Override
    public WeoStatisticsVO getStatisticsSummary() {
        // 优先读Redis缓存(应用启动时由WeoStatisticsComponent预加载), 缓存未命中时委托统计组件回源查库并回写缓存
        Object cache = redisUtil.get(WeoStatisticsComponent.STATISTICS_CACHE_KEY);
        if (cache != null) {
            return JSON.parseObject(cache.toString(), WeoStatisticsVO.class);
        }
        return weoStatisticsComponent.refreshStatisticsCache();
    }

    /**
     * 组装指标元信息VO(附带主题中文名)
     */
    private IndicatorPageVO buildIndicatorPageVO(WeoIndicator indicator, Map<String, String> topicCnMap) {
        IndicatorPageVO vo = new IndicatorPageVO();
        vo.setId(indicator.getId());
        vo.setIndicatorCode(indicator.getIndicatorCode());
        vo.setIndicatorName(indicator.getIndicatorEn());
        vo.setIndicatorNameCn(indicator.getIndicatorCn());
        vo.setTopicCode(indicator.getTopicCode());
        vo.setTopicCn(topicCnMap.get(indicator.getTopicCode()));
        vo.setUnit(indicator.getUnit());
        vo.setScale(indicator.getScale());
        return vo;
    }

    /**
     * 组装国家/经济体信息VO
     */
    private CountryPageVO buildCountryPageVO(WeoCountry country) {
        CountryPageVO vo = new CountryPageVO();
        vo.setCountryCode(country.getCountryCode());
        vo.setCountryEn(country.getCountryEn());
        vo.setCountryCn(country.getCountryCn());
        vo.setIsAggregate(country.getIsAggregate());
        return vo;
    }

    /**
     * 组装国家分组树一级节点
     */
    private CountryTreeVO buildCountryGroup(String groupCode, String groupName, String groupNameCn, List<CountryTreeVO.CountryNode> children) {
        CountryTreeVO node = new CountryTreeVO();
        node.setGroupCode(groupCode);
        node.setGroupName(groupName);
        node.setGroupNameCn(groupNameCn);
        node.setChildren(children);
        return node;
    }

    /**
     * 主题中文名映射: 主题小表一次全量查询
     *
     * @return key=主题编码(topic_en), value=主题中文名
     */
    private Map<String, String> queryTopicCnMap() {
        QueryWrapper<WeoTopic> wrapper = new QueryWrapper<>();
        wrapper.select("topic_en", "topic_cn");
        Map<String, String> nameMap = new HashMap<>();
        for (WeoTopic topic : weoTopicService.list(wrapper)) {
            nameMap.put(topic.getTopicEn(), topic.getTopicCn());
        }
        return nameMap;
    }

    /**
     * 查询国家名称映射(中文名优先, 无中文名回退英文名)
     *
     * @param countryCodes 国家编码集合
     * @return key=国家编码, value=展示名称
     */
    private Map<String, String> queryCountryNameMap(Set<String> countryCodes) {
        QueryWrapper<WeoCountry> wrapper = new QueryWrapper<>();
        wrapper.select("country_code", "country_en", "country_cn").in("country_code", countryCodes);
        Map<String, String> nameMap = new HashMap<>();
        for (WeoCountry country : weoCountryService.list(wrapper)) {
            nameMap.put(country.getCountryCode(),
                    StringUtils.defaultIfBlank(country.getCountryCn(), country.getCountryEn()));
        }
        return nameMap;
    }

    /**
     * 查询指标名称映射(中文名优先, 无中文名回退英文名)
     *
     * @param indicatorCodes 指标编码集合
     * @return key=指标编码, value=展示名称
     */
    private Map<String, String> queryIndicatorNameMap(Set<String> indicatorCodes) {
        QueryWrapper<WeoIndicator> wrapper = new QueryWrapper<>();
        wrapper.select("indicator_code", "indicator_en", "indicator_cn").in("indicator_code", indicatorCodes);
        Map<String, String> nameMap = new HashMap<>();
        for (WeoIndicator indicator : weoIndicatorService.list(wrapper)) {
            nameMap.put(indicator.getIndicatorCode(),
                    StringUtils.defaultIfBlank(indicator.getIndicatorCn(), indicator.getIndicatorEn()));
        }
        return nameMap;
    }

    /**
     * 查询有效【指标-国家】组合: 直接查t_weo_data实际数据取去重组合
     * 查询走(country_code, indicator_code)联合唯一索引, 指标数<=10且国家数<=10, 去重组合上限100个, 开销可控
     *
     * @param indicatorCodeSet 指标编码集合
     * @param countryCodeSet   国家编码集合
     * @return key=指标编码, value=该指标在t_weo_data中有实际数据的国家编码集合
     */
    private Map<String, Set<String>> queryValidComboMap(Set<String> indicatorCodeSet, Set<String> countryCodeSet) {
        QueryWrapper<WeoData> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT indicator_code", "country_code")
                .in("indicator_code", indicatorCodeSet)
                .in("country_code", countryCodeSet);
        Map<String, Set<String>> validMap = new HashMap<>();
        for (WeoData data : weoDataMapper.selectList(wrapper)) {
            if (StringUtils.isNotBlank(data.getCountryCode())) {
                validMap.computeIfAbsent(data.getIndicatorCode(), key -> new HashSet<>())
                        .add(data.getCountryCode());
            }
        }
        return validMap;
    }

    /**
     * 面板查询参数强校验: 列表非空且不超上限、元素非空、年份区间合法、viewType合法
     * 使用hutool Assert断言, 条件不满足抛JeecgBootException, 由全局异常处理器统一转Result.error返回
     *
     * @param dto 查询入参
     * @return 规范化后的viewType(空默认TABLE_LONG)
     */
    private String validateAndNormalize(WeoDataQueryDTO dto) {
        if (dto == null) {
            throw new JeecgBootException("查询参数不能为空");
        }
        if (dto.getIndicatorCodeList() == null || dto.getIndicatorCodeList().isEmpty()) {
            throw new JeecgBootException("指标编码列表不能为空");
        }
        if (dto.getIndicatorCodeList().size() > MAX_INDICATOR_COUNT) {
            throw new JeecgBootException("选中指标过多(最多" + MAX_INDICATOR_COUNT + "个), 请减少指标数量或使用数据导出功能");
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
        if (!VIEW_TYPE_TABLE_LONG.equals(viewType) && !VIEW_TYPE_TABLE_WIDE.equals(viewType)
                && !VIEW_TYPE_TIME_SERIES.equals(viewType) && !VIEW_TYPE_CHART.equals(viewType)) {
            throw new JeecgBootException("viewType不合法, 仅支持: TABLE_LONG/TABLE_WIDE/TIME_SERIES/CHART");
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
     * 分页参数规范化: 页码最小1
     */
    private int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    /**
     * 分页参数规范化: 每页条数1~100
     */
    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /**
     * 聚合查询listMaps结果null元素过滤: 全NULL聚合行会映射出null元素, 遍历前先剔除
     *
     * @param listMaps MyBatis-Plus listMaps结果
     * @return 非null行列表
     */
    private List<Map<String, Object>> safeListMaps(List<Map<String, Object>> listMaps) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (listMaps == null) {
            return rows;
        }
        for (Map<String, Object> row : listMaps) {
            if (row != null) {
                rows.add(row);
            }
        }
        return rows;
    }

    /**
     * 组装长表视图(TABLE_LONG): 国家,指标,年份,数值
     *
     * @param dataRows         底层数据行(已按国家,指标,年份排序)
     * @param countryNameMap   国家名称映射
     * @param indicatorNameMap 指标名称映射
     * @return 长表行列表
     */
    private List<WeoDataLongRowVO> buildLongRows(List<WeoData> dataRows, Map<String, String> countryNameMap, Map<String, String> indicatorNameMap) {
        List<WeoDataLongRowVO> longRows = new ArrayList<>(dataRows.size());
        for (WeoData data : dataRows) {
            WeoDataLongRowVO row = new WeoDataLongRowVO();
            row.setCountryCode(data.getCountryCode());
            row.setCountryName(countryNameMap.get(data.getCountryCode()));
            row.setIndicatorCode(data.getIndicatorCode());
            row.setIndicatorName(indicatorNameMap.get(data.getIndicatorCode()));
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
     * @param dataRows         底层数据行(长表)
     * @param countryNameMap   国家名称映射
     * @param indicatorNameMap 指标名称映射
     * @return 宽表结构
     */
    private WeoDataWideVO buildWideTable(List<WeoData> dataRows, Map<String, String> countryNameMap, Map<String, String> indicatorNameMap) {
        // 1. 列 = 数据中出现过的年份并集(升序)
        TreeSet<Integer> yearSet = new TreeSet<>();
        for (WeoData data : dataRows) {
            yearSet.add(data.getYear());
        }
        List<Integer> yearColumns = new ArrayList<>(yearSet);
        // 2. 按国家×指标分组(LinkedHashMap保持数据出现顺序), 收集各行(指标,年份)->值
        Map<String, WeoDataWideVO.WideRow> rowMap = new LinkedHashMap<>();
        Map<String, Map<Integer, BigDecimal>> valueMap = new HashMap<>();
        for (WeoData data : dataRows) {
            String groupKey = data.getCountryCode() + GROUP_KEY_SEPARATOR + data.getIndicatorCode();
            rowMap.computeIfAbsent(groupKey, key -> {
                WeoDataWideVO.WideRow row = new WeoDataWideVO.WideRow();
                row.setCountryCode(data.getCountryCode());
                row.setCountryName(countryNameMap.get(data.getCountryCode()));
                row.setIndicatorCode(data.getIndicatorCode());
                row.setIndicatorName(indicatorNameMap.get(data.getIndicatorCode()));
                return row;
            });
            valueMap.computeIfAbsent(groupKey, key -> new HashMap<>()).put(data.getYear(), data.getValue());
        }
        // 3. 按年份列对齐输出单元格值(缺失年份为null)
        List<WeoDataWideVO.WideRow> rows = new ArrayList<>(rowMap.size());
        for (Map.Entry<String, WeoDataWideVO.WideRow> entry : rowMap.entrySet()) {
            Map<Integer, BigDecimal> yearValueMap = valueMap.get(entry.getKey());
            List<BigDecimal> values = new ArrayList<>(yearColumns.size());
            for (Integer year : yearColumns) {
                values.add(yearValueMap.get(year));
            }
            entry.getValue().setValues(values);
            rows.add(entry.getValue());
        }
        // 4. 组装宽表
        WeoDataWideVO wide = new WeoDataWideVO();
        wide.setYearColumns(yearColumns);
        wide.setRows(rows);
        return wide;
    }

    /**
     * 组装指标时序视图(TIME_SERIES): 每个国家仅一行, 一级表头=指标, 二级表头=年份
     * 行=国家(仅返回有数据的国家, 按国家编码排序), 列=指标×年份, 单元格=该国家该指标该年份的数值
     *
     * @param dataRows         底层数据行(长表)
     * @param countryNameMap   国家名称映射
     * @param indicatorNameMap 指标名称映射
     * @param indicatorCodes   有效指标编码(保持用户查询顺序, 作为一级表头顺序)
     * @return 指标时序结构
     */
    private WeoDataTimeSeriesVO buildTimeSeriesTable(List<WeoData> dataRows, Map<String, String> countryNameMap,
            Map<String, String> indicatorNameMap, Set<String> indicatorCodes) {
        // 1. 年份子表头 = 数据中出现过的年份并集(升序, 各指标组二级表头一致)
        TreeSet<Integer> yearSet = new TreeSet<>();
        for (WeoData data : dataRows) {
            yearSet.add(data.getYear());
        }
        List<Integer> yearColumns = new ArrayList<>(yearSet);
        // 2. 指标一级表头(按用户查询顺序)
        List<WeoDataTimeSeriesVO.IndicatorColumn> indicators = new ArrayList<>(indicatorCodes.size());
        for (String code : indicatorCodes) {
            WeoDataTimeSeriesVO.IndicatorColumn column = new WeoDataTimeSeriesVO.IndicatorColumn();
            column.setIndicatorCode(code);
            column.setIndicatorName(indicatorNameMap.get(code));
            indicators.add(column);
        }
        // 3. 每个国家一行: 收集(国家 -> (指标 -> (年份 -> 值))), 行顺序按数据出现顺序(SQL已按国家编码排序)
        Map<String, WeoDataTimeSeriesVO.TimeSeriesRow> rowMap = new LinkedHashMap<>();
        Map<String, Map<String, Map<Integer, BigDecimal>>> cellMap = new HashMap<>();
        for (WeoData data : dataRows) {
            String countryCode = data.getCountryCode();
            rowMap.computeIfAbsent(countryCode, key -> {
                WeoDataTimeSeriesVO.TimeSeriesRow row = new WeoDataTimeSeriesVO.TimeSeriesRow();
                row.setCountryCode(countryCode);
                row.setCountryName(countryNameMap.get(countryCode));
                return row;
            });
            cellMap.computeIfAbsent(countryCode, key -> new HashMap<>())
                    .computeIfAbsent(data.getIndicatorCode(), key -> new HashMap<>())
                    .put(data.getYear(), data.getValue());
        }
        // 4. 按指标×年份列对齐输出单元格值(缺失指标/年份为null)
        List<WeoDataTimeSeriesVO.TimeSeriesRow> rows = new ArrayList<>(rowMap.size());
        for (Map.Entry<String, WeoDataTimeSeriesVO.TimeSeriesRow> entry : rowMap.entrySet()) {
            Map<String, Map<Integer, BigDecimal>> indYearMap = cellMap.get(entry.getKey());
            Map<String, List<BigDecimal>> values = new LinkedHashMap<>();
            for (String indCode : indicatorCodes) {
                Map<Integer, BigDecimal> yearValueMap = indYearMap.get(indCode);
                List<BigDecimal> cellValues = new ArrayList<>(yearColumns.size());
                for (Integer year : yearColumns) {
                    cellValues.add(yearValueMap == null ? null : yearValueMap.get(year));
                }
                values.put(indCode, cellValues);
            }
            entry.getValue().setValues(values);
            rows.add(entry.getValue());
        }
        // 5. 组装指标时序
        WeoDataTimeSeriesVO timeSeries = new WeoDataTimeSeriesVO();
        timeSeries.setYearColumns(yearColumns);
        timeSeries.setIndicators(indicators);
        timeSeries.setRows(rows);
        return timeSeries;
    }

    /**
     * 组装图表视图(CHART): ECharts折线/柱状图可直接使用的结构化数据
     * xAxis.data=years(升序), 每条系列=一个国家×指标组合, 缺失年份为null(ECharts自动断点)
     *
     * @param dataRows         底层数据行(长表)
     * @param countryNameMap   国家名称映射
     * @param indicatorNameMap 指标名称映射
     * @return 图表结构
     */
    private WeoDataChartVO buildChartData(List<WeoData> dataRows, Map<String, String> countryNameMap, Map<String, String> indicatorNameMap) {
        // 1. X轴 = 数据中出现过的年份并集(升序)
        TreeSet<Integer> yearSet = new TreeSet<>();
        for (WeoData data : dataRows) {
            yearSet.add(data.getYear());
        }
        List<Integer> years = new ArrayList<>(yearSet);
        // 2. 按国家×指标分组构建系列(LinkedHashMap保持数据出现顺序)
        Map<String, WeoDataChartVO.ChartSeries> seriesMap = new LinkedHashMap<>();
        Map<String, Map<Integer, BigDecimal>> valueMap = new HashMap<>();
        for (WeoData data : dataRows) {
            String groupKey = data.getCountryCode() + GROUP_KEY_SEPARATOR + data.getIndicatorCode();
            seriesMap.computeIfAbsent(groupKey, key -> {
                WeoDataChartVO.ChartSeries chartSeries = new WeoDataChartVO.ChartSeries();
                String countryName = countryNameMap.get(data.getCountryCode());
                String indicatorName = indicatorNameMap.get(data.getIndicatorCode());
                chartSeries.setCountryCode(data.getCountryCode());
                chartSeries.setCountryName(countryName);
                chartSeries.setIndicatorCode(data.getIndicatorCode());
                chartSeries.setIndicatorName(indicatorName);
                chartSeries.setName(StringUtils.defaultIfBlank(indicatorName, data.getIndicatorCode())
                        + "-" + StringUtils.defaultIfBlank(countryName, data.getCountryCode()));
                return chartSeries;
            });
            valueMap.computeIfAbsent(groupKey, key -> new HashMap<>()).put(data.getYear(), data.getValue());
        }
        // 3. 按X轴年份对齐系列值(缺失年份为null)
        for (Map.Entry<String, WeoDataChartVO.ChartSeries> entry : seriesMap.entrySet()) {
            Map<Integer, BigDecimal> yearValueMap = valueMap.get(entry.getKey());
            List<BigDecimal> seriesData = new ArrayList<>(years.size());
            for (Integer year : years) {
                seriesData.add(yearValueMap.get(year));
            }
            entry.getValue().setData(seriesData);
        }
        // 4. 组装图表结构
        WeoDataChartVO chart = new WeoDataChartVO();
        chart.setYears(years);
        chart.setSeriesList(new ArrayList<>(seriesMap.values()));
        return chart;
    }
}
