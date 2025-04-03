package com.xmq.util;

import java.util.UUID;

public class StringUtils {
    
    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 生成UUID（去掉横线）
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 截取字符串
     */
    public static String substring(String str, int maxLength) {
        if (isEmpty(str)) {
            return str;
        }
        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }
    
    /**
     * 生成文章摘要
     */
    public static String generateSummary(String content, int maxLength) {
        if (isEmpty(content)) {
            return "";
        }
        // 去除HTML标签
        content = content.replaceAll("<[^>]+>", "");
        // 去除Markdown标记
        content = content.replaceAll("[#*`~_>]", "");
        // 截取
        return substring(content, maxLength);
    }
} 