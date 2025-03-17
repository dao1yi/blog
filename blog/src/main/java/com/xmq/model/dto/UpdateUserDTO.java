package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
@Schema(description = "修改用户信息请求参数")
public class UpdateUserDTO {
    
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
//    @Length(min = 4, max = 16, message = "用户名长度必须在4-16个字符之间")
    private String username;
    
    @Schema(description = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "角色", allowableValues = {"USER", "ADMIN"})
    @NotBlank(message = "角色不能为空")
    private String role;
} 