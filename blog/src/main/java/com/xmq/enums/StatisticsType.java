package com.xmq.enums;

import lombok.Getter;

@Getter
public enum StatisticsType {
    DAY("day", "按天统计"),
    WEEK("week", "按周统计"),
    MONTH("month", "按月统计"),
    YEAR("year", "按年统计");

    private final String code;
    private final String desc;

    StatisticsType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatisticsType fromCode(String code) {
        for (StatisticsType type : StatisticsType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 