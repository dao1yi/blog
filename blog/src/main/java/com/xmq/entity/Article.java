package com.xmq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName article
 */
@TableName(value ="article")
@Data
public class Article implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String summary;

    private String coverImage;

    private Integer viewCount;

    private Integer commentCount;

    private Integer likeCount;

    private Integer collectionCount;

    private Integer status;

    private Long authorId;

    private Integer isTop;

    private Integer isBroadcast;

    private Date createdTime;

    private Date updatedTime;

    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}