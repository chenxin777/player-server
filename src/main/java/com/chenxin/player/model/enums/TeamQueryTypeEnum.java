package com.chenxin.player.model.enums;

/**
 * @author fangchenxin
 * @description 队伍查询类型枚举
 * @date 2024/5/13 23:24
 * @modify
 */
public enum TeamQueryTypeEnum {

    NO_EXPIRED(0, "未过期"),
    EXPIRED(1, "全部");

    private Integer value;

    private String text;

    TeamQueryTypeEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static TeamQueryTypeEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        TeamQueryTypeEnum[] values = TeamQueryTypeEnum.values();
        for (TeamQueryTypeEnum teamQueryTypeEnum : values) {
            if (teamQueryTypeEnum.value == value) {
                return teamQueryTypeEnum;
            }
        }
        return null;
    }
}
