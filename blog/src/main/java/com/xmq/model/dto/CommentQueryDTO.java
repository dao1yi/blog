package com.xmq.model.dto;

import lombok.Data;

@Data
public class CommentQueryDTO {
    
    private Long articleId;  // 文章ID
    private Long parentId;   // 父评论ID
    private Integer page;    // 页码
    private Integer size;    // 每页记录数
} 