package com.xmq.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateDTO {
    
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字符")
    private String content;
    
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    private Long parentId;  // 父评论ID，可以为空
} 