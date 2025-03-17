package com.xmq.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息响应")
public class UserVO {
    
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "个人简介")
    private String bio;
    
    @Schema(description = "注册时间")
    private LocalDateTime createTime;
    
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
} 