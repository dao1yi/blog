package com.xmq.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文件信息响应")
public class FileVO {

    /**
     * 文件唯一标识
     */
    @Schema(description = "文件ID")
    private String id;

    /**
     * 上传时的原始文件名
     */
    @Schema(description = "原始文件名")
    private String originalFilename;

    /**
     * 文件大小，单位：字节
     */
    @Schema(description = "文件大小(字节)")
    private Long size;

    /**
     * 文件MIME类型
     */
    @Schema(description = "文件类型")
    private String contentType;

    /**
     * 文件访问URL
     */
    @Schema(description = "文件访问URL")
    private String url;

    /**
     * 文件用途：avatar-头像，article-文章图片等
     */
    @Schema(description = "文件用途类型(avatar/article等)")
    private String type;

    /**
     * 文件上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime createTime;

    /**
     * 上传文件的用户ID
     */
    @Schema(description = "上传用户ID")
    private Long userId;
} 