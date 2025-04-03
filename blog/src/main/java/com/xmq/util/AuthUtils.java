package com.xmq.util;

import com.xmq.common.ResultCode;
import com.xmq.exception.BusinessException;
import com.xmq.model.LoginUser;

public class AuthUtils {
    
    private static final ThreadLocal<LoginUser> currentUser = new ThreadLocal<>();
    
    /**
     * 设置当前登录用户
     */
    public static void setCurrentUser(LoginUser user) {
        currentUser.set(user);
    }
    
    /**
     * 获取当前登录用户
     */
    public static LoginUser getCurrentUser() {
        LoginUser user = currentUser.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }
    
    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentUser().getRole());
    }
    
    /**
     * 清除当前用户信息
     */
    public static void clear() {
        currentUser.remove();
    }
} 