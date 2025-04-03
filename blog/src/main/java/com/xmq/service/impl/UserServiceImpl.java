package com.xmq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmq.common.ResultCode;
import com.xmq.constant.RedisConstants;
import com.xmq.entity.User;
import com.xmq.exception.BusinessException;
import com.xmq.mapper.UserMapper;
import com.xmq.model.LoginUser;
import com.xmq.model.dto.LoginDTO;
import com.xmq.model.dto.RegisterDTO;
import com.xmq.model.dto.UpdateUserDTO;
import com.xmq.model.dto.UpdateUserInfoDTO;
import com.xmq.service.UserService;
import com.xmq.util.AuthUtils;
import com.xmq.util.EmailUtils;
import com.xmq.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
* @author xumengqi
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-02-25 22:22:23
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void sendCode(String email) {
        // 1. 检查邮箱是否已注册
        if (isEmailExists(email)) {
            throw new BusinessException(ResultCode.EMAIL_EXISTED);
        }

        // 2. 检查发送频率限制
        String codeKey = RedisConstants.CODE_KEY_PREFIX + email;
        Long ttl = redisTemplate.getExpire(codeKey);

        if (ttl != null && ttl > (RedisConstants.CODE_EXPIRE_MINUTES * 60 - RedisConstants.SEND_COOLDOWN_SECONDS)) {
            
            // 如果验证码剩余时间大于(有效期-冷却时间)，说明在冷却期内
            throw new BusinessException(ResultCode.PARAM_ERROR, 
                "请" + RedisConstants.SEND_COOLDOWN_SECONDS + "秒后再试");
        }

        // 3. 生成验证码
        String code = EmailUtils.generateVerificationCode();

        // 4. 发送验证码邮件
        try {
            emailUtils.sendVerificationCode(email, code);
        } catch (Exception e) {
            log.error("发送验证码失败: {}", email, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "验证码发送失败");
        }

        // 5. 将验证码保存到Redis，设置5分钟有效期
        redisTemplate.opsForValue().set(codeKey, code, 
            RedisConstants.CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        if (isUsernameExists(registerDTO.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTED);
        }

        // 2. 检查邮箱是否已存在
        if (isEmailExists(registerDTO.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_EXISTED);
        }

        // 3. 验证验证码
        String codeKey = RedisConstants.CODE_KEY_PREFIX + registerDTO.getEmail();
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        if (savedCode == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码已过期");
        }
        if (!savedCode.equals(registerDTO.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误");
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 使用MD5加密密码
        user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));
        user.setEmail(registerDTO.getEmail());
        user.setRole("USER"); // 默认角色
        user.setStatus(1); // 默认启用
        // 5. 保存用户
        save(user);

        // 6. 删除验证码
        redisTemplate.delete(codeKey);
    }

    @Override
    public String login(LoginDTO loginDTO) {
        // 1. 根据账号查询用户
        User user = lambdaQuery()
                .and(wrapper -> wrapper
                    .eq(User::getUsername, loginDTO.getAccount())
                    .or()
                    .eq(User::getEmail, loginDTO.getAccount())
                )
                .one();
        
        // 2. 用户不存在
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 3. 校验账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "账号已被禁用");
        }
        
        // 4. 校验密码
        String encodedPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!encodedPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 5. 生成token
        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRole(user.getRole());
        
        return JwtUtils.generateToken(loginUser);
    }

    /**
     * 检查用户名是否已存在
     */
    private boolean isUsernameExists(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .exists();
    }

    /**
     * 检查邮箱是否已存在
     */
    private boolean isEmailExists(String email) {
        return lambdaQuery()
                .eq(User::getEmail, email)
                .exists();
    }

    @Override
    public User getUserInfo() {
        // 1. 获取当前登录用户ID
        Long userId = AuthUtils.getCurrentUserId();
        
        // 2. 查询用户信息
        User user = getById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 3. 清除敏感信息
        user.setPassword(null);
        user.setIsDeleted(null);
        
        return user;
    }

    @Override
    public IPage<User> getUserList(Integer page, Integer size, String username, String email, Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
            .like(StringUtils.hasText(username), User::getUsername, username)
            .like(StringUtils.hasText(email), User::getEmail, email)
            .eq(status != null, User::getStatus, status)
            .orderByDesc(User::getCreatedTime);
            
        return userMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        // 1. 检查用户是否存在
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 2. 验证状态值
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的状态值");
        }
        
        // 3. 更新用户状态
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setStatus(status);
        updateById(updateUser);
    }
    
    @Override
    public void deleteUser(Long id) {
        // 1. 检查用户是否存在
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 2. 检查是否为超级管理员
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能删除管理员账号");
        }
        
        // 3. 删除用户（逻辑删除）
        removeById(id);
    }

    @Override
    public void updateUser(Long id, UpdateUserDTO updateUserDTO) {
        // 1. 检查用户是否存在
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 2. 检查用户名是否已被其他用户使用
        boolean usernameExists = lambdaQuery()
                .eq(User::getUsername, updateUserDTO.getUsername())
                .ne(User::getId, id)
                .exists();
        if (usernameExists) {
            throw new BusinessException(ResultCode.USERNAME_EXISTED);
        }
        
        // 3. 检查邮箱是否已被其他用户使用
        boolean emailExists = lambdaQuery()
                .eq(User::getEmail, updateUserDTO.getEmail())
                .ne(User::getId, id)
                .exists();
        if (emailExists) {
            throw new BusinessException(ResultCode.EMAIL_EXISTED);
        }
        
        // 4. 验证角色是否有效
        if (!Arrays.asList("USER", "ADMIN").contains(updateUserDTO.getRole())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的角色值");
        }
        
        // 5. 更新用户信息
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setUsername(updateUserDTO.getUsername());
        updateUser.setEmail(updateUserDTO.getEmail());
        updateUser.setRole(updateUserDTO.getRole());
        
        updateById(updateUser);
    }

    @Override
    public void updateUserInfo(Long id, UpdateUserInfoDTO updateUserInfoDTO) {
        User user = this.getById(id);
        if(user==null){
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
//        更改头像
        user.setAvatar(updateUserInfoDTO.getAvatar());
        user.setIsReceivePush(updateUserInfoDTO.getIsReceivePush());
        this.updateById(user);
    }
}




