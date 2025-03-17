package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
// import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户登录请求")
public class LoginDTO {
    
    @Schema(description = "账号(用户名或邮箱)", required = true)
    // @NotBlank(message = "账号不能为空")
    private String account;
    
    @Schema(description = "密码", required = true)
    // @NotBlank(message = "密码不能为空")
    private String password;
} 