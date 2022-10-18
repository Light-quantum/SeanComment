package com.sean.comment.controller.interceptor;

import com.sean.comment.dto.UserDTO;
import com.sean.comment.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 二级拦截器：拦截需要登录的业务请求，判断是否已经登录
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 ThreadLocal 中获取 User
        final UserDTO userDTO = UserHolder.getUser();
        if(userDTO == null){
            response.setStatus(401); // 未授权
            return false;
        }
        // 放行
        return true;
    }
}
