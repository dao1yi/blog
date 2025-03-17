package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建文章请求")
public class ArticleCreateDTO {
    
    @Schema(description = "文章标题", required = true)
    private String title;
    
    @Schema(description = "文章内容", required = true)
    private String content;
    
    @Schema(description = "文章摘要")
    private String summary;
    
    @Schema(description = "封面图片URL")
    private String coverImage;
    
    @Schema(description = "状态：0草稿，1已发布", defaultValue = "1")
    private Integer status = 1;
} 