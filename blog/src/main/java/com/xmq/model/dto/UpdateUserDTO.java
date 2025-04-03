package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
@Schema(description = "修改用户信息请求参数")
public class UpdateUserDTO {
    
    /**
     * 用户名，长度1-50个字符
     */
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Length(min = 1, max = 50, message = "用户名长度必须在1-50个字符之间")
    private String username;
    
    /**
     * 用户邮箱地址
     */
    @Schema(description = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 用户角色：USER-普通用户，ADMIN-管理员
     */
    @Schema(description = "角色", allowableValues = {"USER", "ADMIN"})
    @NotBlank(message = "角色不能为空")
    private String role;
} 