package org.jeecg.modules.weo.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.weo.entity.WeoData;
import org.jeecg.modules.weo.mapper.WeoDataMapper;
import org.jeecg.modules.weo.service.IWeoDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 世界经济组织数据表
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class WeoDataServiceImpl extends ServiceImpl<WeoDataMapper, WeoData> implements IWeoDataService {

    /**固定列: 第5列(索引4)=国家编码, 第8列(索引7)=指标编码*/
    private static final int COL_COUNTRY_CODE = 4;
    private static final int COL_INDICATOR_CODE = 7;
    /**固定年份列: 第32列(索引31)=1980年, 至第83列(索引82)=2031年*/
    private static final int COL_YEAR_START = 31;
    private static final int COL_YEAR_END = 82;
    private static final int START_YEAR = 1980;

    /**分块事务大小: 每1万条独立commit一次, 避免大数据量单事务撑爆数据库redo日志*/
    private static final int CHUNK_SIZE = 10000;
    /**saveBatch的JDBC批次大小*/
    private static final int BATCH_SIZE = 1000;

    private final DataFormatter dataFormatter = new DataFormatter();

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * 导入IMF WEO宽表Excel, 按固定列读取:
     * 第5列(索引4)=国家编码, 第8列(索引7)=指标编码, 第32列(索引31)~第83列(索引82)依次为1980~2031年数据
     * 一行数据遍历年份列生成长表记录, 年份值为空的数据不入库
     * 大数据量分块提交: 每1万条独立事务commit一次, 失败仅回滚当前块, 数据库redo日志压力恒定
     *
     * @param file excel文件
     */
    @Override
    public void importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new JeecgBootException("上传文件为空");
        }
        long startTime = System.currentTimeMillis();
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 1. 定位表头行: 前10行内取第一个非空行作为表头
            Row headerRow = null;
            int startRows = 0;
            for (int i = 0; i < Math.min(10, sheet.getLastRowNum() + 1); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getLastCellNum() > 0) {
                    headerRow = row;
                    startRows = i + 1;
                    break;
                }
            }
            if (headerRow == null) {
                throw new JeecgBootException("未找到表头行");
            }
            // 2. 构建固定年份列映射: 第32列~第83列依次对应1980~2031年
            Map<Integer, Integer> yearColumns = new LinkedHashMap<>();
            for (int c = COL_YEAR_START; c <= COL_YEAR_END; c++) {
                yearColumns.put(c, START_YEAR + (c - COL_YEAR_START));
            }
            // 3. 逐行读取: 固定列取国家/指标代码, 遍历年份列, 一行生成多条记录, 值为空不入库
            List<WeoData> chunk = new ArrayList<>(CHUNK_SIZE);
            long total = 0;
            for (int r = startRows; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String countryCode = readText(row.getCell(COL_COUNTRY_CODE));
                String indicatorCode = readText(row.getCell(COL_INDICATOR_CODE));
                if (countryCode.isEmpty() || indicatorCode.isEmpty()) {
                    continue;
                }
                for (Map.Entry<Integer, Integer> entry : yearColumns.entrySet()) {
                    BigDecimal value = readValue(row.getCell(entry.getKey()));
                    if (value == null) {
                        continue;
                    }
                    WeoData data = new WeoData();
                    data.setCountryCode(countryCode);
                    data.setIndicatorCode(indicatorCode);
                    data.setYear(entry.getValue());
                    data.setValue(value);
                    chunk.add(data);
                    // 4. 分块提交: 每凑满1万条独立事务commit一次
                    if (chunk.size() >= CHUNK_SIZE) {
                        total += saveChunk(txTemplate, chunk);
                        log.info("WEO导入进度: 已入库{}条, 当前Excel第{}/{}行", total, r, sheet.getLastRowNum());
                    }
                }
            }
            // 5. 提交尾部不足1万条的剩余数据
            if (!chunk.isEmpty()) {
                total += saveChunk(txTemplate, chunk);
            }
            log.info("WEO数据导入完成: 共{}条, 耗时{}秒", total, (System.currentTimeMillis() - startTime) / 1000);
        } catch (JeecgBootException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            log.error("WEO数据导入失败", e);
            throw new JeecgBootException("Excel解析失败: " + e.getMessage());
        }
    }

    /**
     * 分块入库: 每块独立事务, saveBatch按1000条JDBC批量执行
     */
    private long saveChunk(TransactionTemplate txTemplate, List<WeoData> chunk) {
        int size = chunk.size();
        txTemplate.executeWithoutResult(status -> this.saveBatch(chunk, BATCH_SIZE));
        chunk.clear();
        return size;
    }

    /**
     * 读取单元格文本
     */
    private String readText(Cell cell) {
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell).trim();
    }

    /**
     * 读取数值单元格, 空白/"-"/".."或无法解析的返回null(不入库)
     */
    private BigDecimal readValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING:
                    String text = cell.getStringCellValue().trim();
                    if (text.isEmpty() || "-".equals(text) || "..".equals(text)) {
                        return null;
                    }
                    return new BigDecimal(text);
                case FORMULA:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
