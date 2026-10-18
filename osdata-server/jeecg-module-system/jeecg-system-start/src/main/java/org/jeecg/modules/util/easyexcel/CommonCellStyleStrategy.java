package org.jeecg.modules.util.easyexcel;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * 导出单元格策略
 */
public class CommonCellStyleStrategy {

    /**
     * 设置单元格样式(仅用于示例)
     *
     * @return 样式策略
     */
    public static HorizontalCellStyleStrategy getHorizontalCellStyleStrategy() {
        // 表头策略
        WriteCellStyle headerCellStyle = new WriteCellStyle();
        // 表头水平对齐居中
        headerCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 背景色
        headerCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        WriteFont headerFont = new WriteFont();
        headerFont.setFontHeightInPoints((short) 15);
        headerCellStyle.setWriteFont(headerFont);
        // 自动换行
        headerCellStyle.setWrapped(Boolean.FALSE);
        // 内容策略
        WriteCellStyle contentCellStyle = new WriteCellStyle();
        // 设置背景色: 需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 不然无法显示背景颜色.头默认了 FillPatternType所以可以不指定
        contentCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        // 设置内容靠左对齐
        contentCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        // 设置字体
        WriteFont contentFont = new WriteFont();
        contentFont.setFontHeightInPoints((short) 12);
        contentCellStyle.setWriteFont(contentFont);
        // 设置自动换行
        contentCellStyle.setWrapped(Boolean.FALSE);
        // 设置边框样式和颜色
        contentCellStyle.setBorderLeft(BorderStyle.THIN);
        contentCellStyle.setBorderTop(BorderStyle.THIN);
        contentCellStyle.setBorderRight(BorderStyle.THIN);
        contentCellStyle.setBorderBottom(BorderStyle.THIN);
        contentCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        contentCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        contentCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        contentCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        // 将格式加入单元格样式策略
        return new HorizontalCellStyleStrategy(headerCellStyle, contentCellStyle);
    }
}
