package com.xmq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private Long articleId;

    private Long userId;

    private Long parentId;

    private Date createdTime;

    private Date updatedTime;

    private Integer isTop;

    @TableLogic
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}