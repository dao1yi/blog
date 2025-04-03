package com.xmq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName view_record
 */
@TableName(value ="view_record")
@Data
public class ViewRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long userId;

    private String ipAddress;

    private Date viewTime;

    @TableLogic
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}