package com.xmq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmq.annotation.NeedLogin;
import com.xmq.annotation.RequireAdmin;
import com.xmq.common.Result;
import com.xmq.entity.User;
import com.xmq.model.LoginUser;
import com.xmq.model.dto.*;
import com.xmq.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "发送验证码", description = "发送邮箱验证码")
    @PostMapping("/code")
    public Result<Void> sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        userService.sendCode(sendCodeDTO.getEmail());
        return Result.success();
    }

    @Operation(summary = "用户注册", description = "用户注册接口")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    @Operation(summary = "用户登录", description = "用户登录接口")
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success(token);
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    @GetMapping("/info")
    @NeedLogin
    public Result<User> getUserInfo() {
        User userInfo = userService.getUserInfo();
        return Result.success(userInfo);
    }

    @Operation(summary = "获取用户列表", description = "分页获取用户列表，需要管理员权限")
    @GetMapping("/list")
    @RequireAdmin
    public Result<IPage<User>> getUserList(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") 
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名关键字") 
            @RequestParam(required = false) String username,
            @Parameter(description = "邮箱关键字") 
            @RequestParam(required = false) String email,
            @Parameter(description = "用户状态: 0-禁用, 1-正常") 
            @RequestParam(required = false) Integer status) {
        
        return Result.success(userService.getUserList(page, size, username, email, status));
    }

    @Operation(summary = "修改用户状态", description = "修改用户状态，需要管理员权限")
    @PutMapping("/{id}/status")
    @RequireAdmin
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") 
            @PathVariable Long id,
            @Parameter(description = "用户状态: 0-禁用, 1-正常") 
            @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除用户", description = "删除指定用户，需要管理员权限")
    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") 
            @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "修改用户信息", description = "修改用户基本信息，需要管理员权限")
    @PutMapping("/{id}")
    @RequireAdmin
    public Result<Void> updateUser(
            @Parameter(description = "用户ID") 
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserDTO updateUserDTO) {
        userService.updateUser(id, updateUserDTO);
        return Result.success();
    }

    @Operation(summary = "修改用户个人信息", description = "修改用户个人信息")
    @PutMapping("/info/{id}")
    @NeedLogin
    public Result<Void> updateUserInfo(
            @Parameter(description = "用户ID")
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserInfoDTO updateUserInfoDTO) {
        userService.updateUserInfo(id, updateUserInfoDTO);
        return Result.success();
    }
}