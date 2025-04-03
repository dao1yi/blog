package com.xmq.model.dto;

import lombok.Data;

@Data
public class CommentQueryDTO {
    
    /**
     * 查询指定文章的评论
     */
    private Long articleId;
    
    /**
     * 查询指定父评论的回复
     */
    private Long parentId;
    
    /**
     * 当前页码，从1开始
     */
    private Integer page;
    
    /**
     * 每页显示记录数
     */
    private Integer size;
} 