package com.xmq.interceptor;

import com.xmq.annotation.NeedLogin;
import com.xmq.annotation.RequireAdmin;
import com.xmq.common.ResultCode;
import com.xmq.exception.BusinessException;
import com.xmq.model.LoginUser;
import com.xmq.util.AuthUtils;
import com.xmq.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行非HandlerMethod请求（如静态资源）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        NeedLogin needLogin = handlerMethod.getMethodAnnotation(NeedLogin.class);
        RequireAdmin requireAdmin = handlerMethod.getMethodAnnotation(RequireAdmin.class);


        try {
            String token = request.getHeader("Authorization");
            if (token == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }

            if (!JwtUtils.validateToken(token)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }

            LoginUser loginUser = JwtUtils.parseToken(token);
            if (loginUser == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }

            AuthUtils.setCurrentUser(loginUser);

            // 检查管理员权限
            if (requireAdmin != null && !AuthUtils.isAdmin()) {
                throw new BusinessException(ResultCode.NO_PERMISSION);
            }
            return true;
        } catch (BusinessException e) {
            throw e; // 直接抛出已知业务异常
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }finally {
            // 不需要登录的接口直接放行
            if (needLogin == null && requireAdmin == null) {
                return true;
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除当前登录用户信息
        AuthUtils.clear();
    }
} 