package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
@Schema(description = "用户信息更新请求")
public class UserUpdateDTO {
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "个人简介")
    private String bio;
} 