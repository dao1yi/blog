package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "发送验证码请求")
public class SendCodeDTO {
    
    /**
     * 接收验证码的邮箱地址
     */
    @Schema(description = "邮箱", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
} 