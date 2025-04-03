package com.xmq.service;

import com.xmq.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmq.model.dto.RegisterDTO;
import com.xmq.model.dto.LoginDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmq.model.dto.UpdateUserDTO;
import com.xmq.model.dto.UpdateUserInfoDTO;

/**
* @author xumengqi
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-02-25 22:22:23
*/
public interface UserService extends IService<User> {
    /**
     * 发送验证码
     * @param email 邮箱
     */
    void sendCode(String email);

    /**
     * 用户注册
     * @param registerDTO 注册信息
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return token
     */
    String login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    User getUserInfo();

    IPage<User> getUserList(Integer page, Integer size, String username, String email, Integer status);

    void updateUserStatus(Long id, Integer status);
    
    void deleteUser(Long id);

    void updateUser(Long id, UpdateUserDTO updateUserDTO);

    void updateUserInfo(Long id, UpdateUserInfoDTO updateUserInfoDTO);
}
