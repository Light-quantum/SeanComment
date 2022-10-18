package com.sean.comment.controller.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.sean.comment.dto.UserDTO;
import com.sean.comment.utils.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sean.comment.utils.RedisConstants.LOGIN_USER_KEY;
import static com.sean.comment.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * 一级拦截器：主要用于拦截所有请求刷新 redis 登陆状态的存在时间
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的 token
        final String token = request.getHeader("authorization");
        if(StringUtils.isBlank(token)){
            return true;
        }
        // 查询 redis 中的用户
        final Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
        // 判断用户是否存在
        if(userMap.isEmpty()){
            return true;
        }
        // 解析用户
        final UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        UserHolder.saveUser(userDTO);
        // 刷新 token 有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
