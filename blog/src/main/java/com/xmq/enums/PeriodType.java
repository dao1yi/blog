package com.xmq.enums;

import lombok.Getter;

@Getter
public enum PeriodType {
    DAY("day", 1, "日榜"),
    WEEK("week", 7, "周榜"),
    MONTH("month", 30, "月榜");

    private final String code;
    private final int days;
    private final String desc;

    PeriodType(String code, int days, String desc) {
        this.code = code;
        this.days = days;
        this.desc = desc;
    }

    public static PeriodType fromCode(String code) {
        for (PeriodType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 