package org.jeecg.modules.api.component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.api.wdi.vo.SeriesYearRangeVO;
import org.jeecg.modules.api.wdi.vo.WdiStatisticsVO;
import org.jeecg.modules.wdi.entity.WdiData;
import org.jeecg.modules.wdi.mapper.WdiDataMapper;
import org.jeecg.modules.wdi.service.IWdiCountryService;
import org.jeecg.modules.wdi.service.IWdiSeriesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @Description: WDI统计信息组件(统计概览查询+启动预加载)
 * 职责1: 应用启动时通过ApplicationRunner触发统计概览的CompletableFuture多线程查询并写入Redis, 接口调用优先读缓存;
 * 职责2: 对外提供queryStatisticsFromDb回源查询与refreshStatisticsCache(查库+回写缓存),
 *        WdiServiceImpl统计概览接口缓存未命中时委托本组件回源
 * 预加载失败不影响应用启动, 接口调用时会自动回源数据库查询并回写缓存
 * @Author: jeecg-boot
 * @Date:   2026-10-16
 * @Version: V1.0
 */
@Slf4j
@Component
public class WdiStatisticsComponent implements ApplicationRunner {

    /**统计概览Redis缓存key(应用启动时预加载, 接口优先读缓存; WdiServiceImpl读缓存引用)*/
    public static final String STATISTICS_CACHE_KEY = "wdi:statistics:summary";
    /**统计概览缓存过期时间: 7天(启动时刷新, 两次启动间近似持久, 避免过期回源触发t_wdi_data大表COUNT)*/
    private static final long STATISTICS_CACHE_SECONDS = 365 * 24 * 3600L;
    /**统计查询专用线程池: 四项统计并行查询(最多占用4个DB连接), 守护线程不阻塞JVM退出*/
    private static final ExecutorService STATISTICS_EXECUTOR = Executors.newFixedThreadPool(4, new ThreadFactory() {

        private final AtomicInteger seq = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "wdi-statistics-" + seq.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    @Autowired
    private IWdiSeriesService wdiSeriesService;
    @Autowired
    private IWdiCountryService wdiCountryService;
    @Resource
    private WdiDataMapper wdiDataMapper;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 启动预加载: 多线程查询四项统计并写入Redis缓存
     * ApplicationRunner在Web容器就绪后执行, 同步执行不阻塞HTTP服务; 异常吞掉仅告警, 保证应用正常启动
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            long beginTime = System.currentTimeMillis();
            refreshStatisticsCache();
            log.info("WDI统计信息启动预加载完成并写入Redis, 耗时={}ms", System.currentTimeMillis() - beginTime);
        } catch (Exception e) {
            log.warn("WDI统计信息启动预加载失败, 接口调用时将回源数据库查询: {}", e.getMessage());
        }
    }

    /**
     * 刷新统计概览缓存: 多线程查库并写入Redis(7天过期), 返回查询结果
     * 启动预加载与WdiServiceImpl统计概览接口缓存未命中回源时共用
     *
     * @return 统计概览
     */
    public WdiStatisticsVO refreshStatisticsCache() {
        WdiStatisticsVO vo = queryStatisticsFromDb();
        redisUtil.set(STATISTICS_CACHE_KEY, JSON.toJSONString(vo), STATISTICS_CACHE_SECONDS);
        return vo;
    }

    /**
     * 统计概览回源查询: 四项统计用CompletableFuture并行执行, 总耗时≈最慢一项(t_wdi_data大表COUNT)
     * 1.指标总数: t_wdi_series行数; 2.国家总数: t_wdi_country行数;
     * 3.年份跨度: t_wdi_series_time全局MIN/MAX(year)(与年份范围接口同源, 小表查询);
     * 4.数据记录总数: t_wdi_data行数(约900w行, 全索引扫描, 只允许走缓存/启动预加载, 禁止高频实时调用)
     *
     * @return 统计概览VO
     */
    public WdiStatisticsVO queryStatisticsFromDb() {
        long beginTime = System.currentTimeMillis();
        CompletableFuture<Long> seriesCountFuture = CompletableFuture.supplyAsync(wdiSeriesService::count, STATISTICS_EXECUTOR);
        CompletableFuture<Long> countryCountFuture = CompletableFuture.supplyAsync(wdiCountryService::count, STATISTICS_EXECUTOR);
        CompletableFuture<Long> dataCountFuture = CompletableFuture.supplyAsync(() -> wdiDataMapper.selectCount(null), STATISTICS_EXECUTOR);
        CompletableFuture<SeriesYearRangeVO> yearSpanFuture = CompletableFuture.supplyAsync(this::queryGlobalYearSpan, STATISTICS_EXECUTOR);
        // 等待全部完成, 失败时解包CompletionException抛业务异常(全局异常处理器统一转Result.error返回)
        try {
            CompletableFuture.allOf(seriesCountFuture, countryCountFuture, dataCountFuture, yearSpanFuture).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("WDI统计概览数据库查询失败", cause);
            throw new JeecgBootException("WDI统计概览查询失败: " + cause.getMessage());
        }
        SeriesYearRangeVO yearSpan = yearSpanFuture.join();
        WdiStatisticsVO vo = new WdiStatisticsVO();
        vo.setSeriesCount(seriesCountFuture.join());
        vo.setCountryCount(countryCountFuture.join());
        vo.setMinYear(yearSpan.getMinYear());
        vo.setMaxYear(yearSpan.getMaxYear());
        vo.setDataCount(dataCountFuture.join());
        log.info("WDI统计概览查询完成: 指标数={}, 国家数={}, 年份跨度=[{},{}], 数据记录数={}, 耗时={}ms",
                vo.getSeriesCount(), vo.getCountryCount(), vo.getMinYear(), vo.getMaxYear(),
                vo.getDataCount(), System.currentTimeMillis() - beginTime);
        return vo;
    }

    /**
     * 查询全部指标的全局年份覆盖范围(t_wdi_series_time聚合MIN/MAX年份)
     * 与querySeriesYearRange年份范围接口同源, t_wdi_series_time为小表, 查询开销可忽略
     *
     * @return 年份范围(表无数据时min/max为null)
     */
    private SeriesYearRangeVO queryGlobalYearSpan() {
        QueryWrapper<WdiData> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("MIN(year) AS minYear", "MAX(year) AS maxYear");
        SeriesYearRangeVO vo = new SeriesYearRangeVO();
        // 注意: 表无数据时MIN/MAX聚合返回一行全NULL, MyBatis selectMaps对整行NULL结果返回null元素, 必须跳过
        for (Map<String, Object> row : wdiDataMapper.selectMaps(queryWrapper)) {
            if (row == null) {
                continue;
            }
            Object min = row.get("minYear");
            Object max = row.get("maxYear");
            if (min != null) {
                vo.setMinYear(Integer.valueOf(min.toString()));
            }
            if (max != null) {
                vo.setMaxYear(Integer.valueOf(max.toString()));
            }
        }
        return vo;
    }
}
