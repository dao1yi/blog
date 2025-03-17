package com.xmq.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文件信息响应")
public class FileVO {

    @Schema(description = "文件ID")
    private String id;

    @Schema(description = "原始文件名")
    private String originalFilename;

    @Schema(description = "文件大小(字节)")
    private Long size;

    @Schema(description = "文件类型")
    private String contentType;

    @Schema(description = "文件访问URL")
    private String url;

    @Schema(description = "文件用途类型(avatar/article等)")
    private String type;

    @Schema(description = "上传时间")
    private LocalDateTime createTime;

    @Schema(description = "上传用户ID")
    private Long userId;
} 