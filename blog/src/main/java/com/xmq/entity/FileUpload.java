package com.xmq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName file_upload
 */
@TableName(value ="file_upload")
@Data
public class FileUpload implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    private Long userId;

    private Date createdTime;

    private Date updatedTime;

    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}