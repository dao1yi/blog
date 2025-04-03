package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
// import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户登录请求")
public class LoginDTO {
    
    /**
     * 登录账号，可以是用户名或邮箱
     */
    @Schema(description = "账号(用户名或邮箱)", required = true)
    @NotBlank(message = "账号不能为空")
    private String account;
    
    /**
     * 登录密码，需要进行加密处理
     */
    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
} 