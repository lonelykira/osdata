package org.jeecg.modules.airag.app.enums;

import org.apache.commons.lang3.StringUtils;

/**
* @Description: 图片大小比例枚举
*
* @author: wangshuai
* @date: 2026/2/4 19:55
*/
public enum ImageSizeEnum {
    SIZE_1024_1024("1024*1024", "1:1"),
    SIZE_1280_720("1280*720", "16:9"),
    SIZE_720_1280("720*1280", "9:16"),
    SIZE_1024_768("1024*768", "4:3"),
    SIZE_768_1024("768*1024", "3:4");

    ImageSizeEnum(String size, String ratio) {
        this.size = size;
        this.ratio = ratio;
    }

    /**
     * 大小
     */
    private String size;
    /**
     * 比例
     */
    private String ratio;

    public String getSize() {
        return size;
    }

    public String getRatio() {
        return ratio;
    }

    /**
     * 根据size获取ratio
     *
     * @param size
     * @return
     */
    public static String getRatioBySize(String size) {
        if (StringUtils.isBlank(size)) {
            return "1:1";
        }
        for (ImageSizeEnum e : ImageSizeEnum.values()) {
            if (e.size.equals(size)) {
                return e.ratio;
            }
        }
        return "1:1";
    }
}
