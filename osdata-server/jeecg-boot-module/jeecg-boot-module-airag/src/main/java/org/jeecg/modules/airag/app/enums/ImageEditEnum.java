package org.jeecg.modules.airag.app.enums;

/**
* @Description: 图像编辑枚举
*
* @author: wangshuai
* @date: 2026/2/28 16:52
*/
public enum ImageEditEnum {
    WANX2_1_IMAGEEDIT("wanx2.1-imageedit"),
    WAN2_5_I2I_PREVIEW("wan2.5-i2i-preview");

    private final String modelName;

    ImageEditEnum(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    /**
     * 检查模型名称是否是图像编辑模型
     * @param modelName 模型名称
     * @return 是否是图像编辑模型
     */
    public static boolean isImageEditModel(String modelName) {
        for (ImageEditEnum model : values()) {
            if (model.getModelName().equals(modelName)) {
                return true;
            }
        }
        return false;
    }
}
