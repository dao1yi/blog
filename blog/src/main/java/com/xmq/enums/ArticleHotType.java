package com.xmq.enums;

import lombok.Getter;

@Getter
public enum ArticleHotType {
    VIEW("view", "浏览量排行"),
    LIKE("like", "点赞量排行");

    private final String code;
    private final String desc;

    ArticleHotType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ArticleHotType fromCode(String code) {
        for (ArticleHotType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 