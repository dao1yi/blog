package com.xmq.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息响应")
public class UserVO {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long id;
    
    /**
     * 用户名，用于登录
     */
    @Schema(description = "用户名")
    private String username;
    
    /**
     * 用户昵称，用于显示
     */
    @Schema(description = "昵称")
    private String nickname;
    
    /**
     * 用户邮箱
     */
    @Schema(description = "邮箱")
    private String email;
    
    /**
     * 用户头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;
    
    /**
     * 个人简介
     */
    @Schema(description = "个人简介")
    private String bio;
    
    /**
     * 注册时间
     */
    @Schema(description = "注册时间")
    private LocalDateTime createTime;
    
    /**
     * 信息最后更新时间
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
} 