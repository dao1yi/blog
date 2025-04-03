package com.xmq.model.vo;

import lombok.Data;
import java.util.Date;

@Data
public class CommentVO {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论者用户ID
     */
    private Long userId;
    
    /**
     * 评论者用户名
     */
    private String userName;
    
    /**
     * 评论者头像URL
     */
    private String userAvatar;


    /**
     * 是否置顶
     */
    private Integer isTop;
    
    /**
     * 父评论ID，如果是一级评论则为null
     */
    private Long parentId;
    
    /**
     * 子评论数量
     */
    private Integer childCount;
    
    /**
     * 评论创建时间
     */
    private Date createdTime;
} 