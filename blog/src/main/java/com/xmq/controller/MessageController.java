package com.xmq.controller;

import com.xmq.annotation.NeedLogin;
import com.xmq.common.Result;
import com.xmq.common.ResultCode;
import com.xmq.entity.User;
import com.xmq.exception.BusinessException;
import com.xmq.service.UserService;
import com.xmq.util.AuthUtils;
import com.xmq.util.EmailUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "留言管理", description = "留言相关接口")
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {


    @Resource
    private EmailUtils emailUtils;

    @Resource
    private UserService userService;

    @Operation(summary = "发布留言", description = "使用邮箱发布留言")
    @PostMapping
    @NeedLogin
    public Result sendMessage(String message){
        Long currentUserId = AuthUtils.getCurrentUserId();
        User user = userService.getById(currentUserId);
        if(user == null){
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        emailUtils.sendMessageByEmail(user.getEmail(),message);
        return Result.success();
    }

}
