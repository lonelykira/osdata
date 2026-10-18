package org.jeecg.modules.wdi.service.impl;

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
import org.jeecg.modules.wdi.entity.WdiData;
import org.jeecg.modules.wdi.mapper.WdiDataMapper;
import org.jeecg.modules.wdi.service.IWdiDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 世行WDI面板数据长表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
@Slf4j
@Service
public class WdiDataServiceImpl extends ServiceImpl<WdiDataMapper, WdiData> implements IWdiDataService {

    /**固定列: Country Name=0, Country Code=1, Indicator Name=2, Indicator Code=3, 年份列从第4列(索引)开始*/
    private static final int COL_COUNTRY_CODE = 1;
    private static final int COL_INDICATOR_CODE = 3;
    private static final int COL_YEAR_START = 4;

    /**分块事务大小: 每1万条独立commit一次, 避免百万级单事务撑爆数据库redo日志*/
    private static final int CHUNK_SIZE = 10000;
    /**saveBatch的JDBC批次大小*/
    private static final int BATCH_SIZE = 1000;

    private final DataFormatter dataFormatter = new DataFormatter();

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * 导入宽表Excel: Country Name | Country Code | Indicator Name | Indicator Code | 1960~2025(多列年份)
     * 前4列为固定字段, 一行数据遍历年份列生成长表记录, 年份值为空的数据不入库
     * 百万级数据分块提交: 每1万条独立事务commit一次, 失败仅回滚当前块, 数据库redo日志压力恒定
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
            // 1. 定位表头行: Country Code 固定在索引1列, 据此在前10行内定位表头
            Row headerRow = null;
            int startRows = 0;
            for (int i = 0; i < Math.min(10, sheet.getLastRowNum()); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    headerRow = row;
                    startRows = i + 1;
                    break;
                }
            }
            if (headerRow == null) {
                throw new JeecgBootException("未找到表头行");
            }
            // 2. 解析年份列: 从索引4列起, 表头为4位数字年份的列纳入遍历
            Map<Integer, Integer> yearColumns = new LinkedHashMap<>();
            for (int c = COL_YEAR_START; c < headerRow.getLastCellNum(); c++) {
                Integer year = parseYear(headerRow.getCell(c));
                if (year != null) {
                    yearColumns.put(c, year);
                }
            }
            if (yearColumns.isEmpty()) {
                throw new JeecgBootException("表头中未识别到年份列");
            }
            // 3. 逐行读取: 前4列固定取国家/指标代码, 遍历年份列, 一行生成多条记录, 值为空不入库
            List<WdiData> chunk = new ArrayList<>(CHUNK_SIZE);
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
                    WdiData data = new WdiData();
                    data.setCountryCode(countryCode);
                    data.setIndicatorCode(indicatorCode);
                    data.setYear(entry.getValue());
                    data.setValue(value);
                    chunk.add(data);
                    // 4. 分块提交: 每凑满1万条独立事务commit一次
                    if (chunk.size() >= CHUNK_SIZE) {
                        total += saveChunk(txTemplate, chunk);
                        log.info("WDI导入进度: 已入库{}条, 当前Excel第{}/{}行", total, r, sheet.getLastRowNum());
                    }
                }
            }
            // 5. 提交尾部不足1万条的剩余数据
            if (!chunk.isEmpty()) {
                total += saveChunk(txTemplate, chunk);
            }
            log.info("WDI面板数据导入完成: 共{}条, 耗时{}秒", total, (System.currentTimeMillis() - startTime) / 1000);
        } catch (JeecgBootException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            log.error("WDI面板数据导入失败", e);
            throw new JeecgBootException("Excel解析失败: " + e.getMessage());
        }
    }

    /**
     * 分块入库: 每块独立事务, saveBatch按1000条JDBC批量执行
     */
    private long saveChunk(TransactionTemplate txTemplate, List<WdiData> chunk) {
        int size = chunk.size();
        txTemplate.executeWithoutResult(status -> this.saveBatch(chunk, BATCH_SIZE));
        chunk.clear();
        return size;
    }

    /**
     * 解析年份列头, 非4位数字年份返回null
     */
    private Integer parseYear(Cell cell) {
        if (cell == null) {
            return null;
        }
        String text = dataFormatter.formatCellValue(cell).trim();
        if (!text.matches("\\d{4}")) {
            return null;
        }
        return Integer.valueOf(text);
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
