package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建文章请求")
public class ArticleCreateDTO {
    
    /**
     * 文章标题
     */
    @Schema(description = "文章标题", required = true)
    private String title;
    
    /**
     * 文章内容，支持 Markdown 格式
     */
    @Schema(description = "文章内容", required = true)
    private String content;
    
    /**
     * 文章摘要，用于列表展示
     */
    @Schema(description = "文章摘要")
    private String summary;
    
    /**
     * 文章封面图片URL地址
     */
    @Schema(description = "封面图片URL")
    private String coverImage;
    
    /**
     * 文章状态：0-草稿，1-已发布
     */
    @Schema(description = "状态：0草稿，1已发布", defaultValue = "1")
    private Integer status = 1;

    @Schema(description = "状态：0正常，1置顶", defaultValue = "0")
    private Integer isTop = 0;

    @Schema(description = "状态：0正常，1广播", defaultValue = "0")
    private Integer isBroadcast = 0;
} 