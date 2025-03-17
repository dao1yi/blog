package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新文章请求")
public class ArticleUpdateDTO {

    @Schema(description = "文章标题")
    @Size(min = 1, max = 100, message = "标题长度必须在1-100之间")
    private String title;

    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "文章封面图URL")
    private String coverImage;

    @Schema(description = "文章分类ID")
    private Long categoryId;

    @Schema(description = "文章标签", example = "['Java', 'Spring']")
    private String[] tags;
} 