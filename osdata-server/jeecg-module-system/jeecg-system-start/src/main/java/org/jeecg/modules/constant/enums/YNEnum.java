package org.jeecg.modules.constant.enums;

/**
 * @author
 * @description 是否删除枚举
 * @date 2022/9/16 10:04:04
 */
public enum YNEnum {
    /**
     * 是否枚举
     */
    YES(1, "是","1"),

    NO(0, "否","0");

    private Integer value;

    private String name;

    private String strValue;

    private YNEnum(Integer value, String name,String strValue){
        this.value = value;
        this.name = name;
        this.strValue = strValue;
    }

    public static Integer getValueByName(String name) {
        for (YNEnum c : YNEnum.values()) {
            if (c.name.equals(name)) {
                return c.value;
            }
        }
        return null;
    }

    public String getStrValue() {
        return this.strValue;
    }
    public String getName() {
        return name;
    }

    public Integer getValue() {
        return value;
    }
}
