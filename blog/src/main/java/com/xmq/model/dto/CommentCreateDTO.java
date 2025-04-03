package com.xmq.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateDTO {
    
    /**
     * 评论内容，最大500字符
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字符")
    private String content;
    
    /**
     * 评论的文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    /**
     * 父评论ID，如果是回复评论则需要传入
     */
    private Long parentId;
} 