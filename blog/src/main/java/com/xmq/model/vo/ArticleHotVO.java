package com.xmq.model.vo;

import lombok.Data;
import java.util.Date;

@Data
public class ArticleHotVO {
    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectionCount;
    private Integer periodCount;
    private Long authorId;
    private String authorName;
    private Date createdTime;
} 