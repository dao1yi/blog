package com.xmq.model.vo;

import lombok.Data;
import java.util.Date;

@Data
public class ArticleHotVO {
    /**
     * 文章ID
     */
    private Long id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章摘要
     */
    private String summary;
    
    /**
     * 文章封面图片URL
     */
    private String coverImage;
    
    /**
     * 文章浏览量
     */
    private Integer viewCount;
    
    /**
     * 文章点赞数
     */
    private Integer likeCount;
    
    /**
     * 文章评论数
     */
    private Integer commentCount;
    
    /**
     * 文章收藏数
     */
    private Integer collectionCount;
    
    /**
     * 统计周期内的计数（浏览量/点赞数/评论数）
     */
    private Integer periodCount;
    
    /**
     * 作者ID
     */
    private Long authorId;
    
    /**
     * 作者名称
     */
    private String authorName;
    
    /**
     * 创建时间
     */
    private Date createdTime;
} 