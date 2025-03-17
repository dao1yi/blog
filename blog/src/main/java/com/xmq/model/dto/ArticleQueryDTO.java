package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文章查询参数")
public class ArticleQueryDTO {
    
    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;
    
    @Schema(description = "每页条数", defaultValue = "10")
    private Integer size = 10;
    
    @Schema(description = "文章标题关键字")
    private String keyword;
    
    @Schema(description = "文章状态：0草稿，1已发布")
    private Integer status;
}