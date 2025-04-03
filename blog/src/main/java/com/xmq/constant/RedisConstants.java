package com.xmq.constant;

/**
 * Redis常量类
 */
public class RedisConstants {
    
    /**
     * 验证码前缀
     */
    public static final String CODE_KEY_PREFIX = "verify_code:";
    
    /**
     * 验证码有效期（分钟）
     */
    public static final long CODE_EXPIRE_MINUTES = 5;
    
    /**
     * 发送冷却时间（秒）
     */
    public static final long SEND_COOLDOWN_SECONDS = 60;

    /**
     * 浏览数据统计前缀
     */
    public static final String VIEW_STATISTICS_PREFIX = "view_statistics:";
    public static final String HOT_ARTICLES_PREFIX = "view_statistics:";


} 