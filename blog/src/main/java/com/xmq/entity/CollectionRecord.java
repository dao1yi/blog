package com.xmq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName collection_record
 */
@TableName(value ="collection_record")
@Data
public class CollectionRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long userId;

    private Date collectionTime;

    private Integer status;

    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}