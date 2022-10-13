package com.sean.comment.controller.interceptor;


import cn.hutool.core.bean.BeanUtil;
import com.sean.comment.dto.UserDTO;
import com.sean.comment.entity.User;
import com.sean.comment.utils.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sean.comment.utils.RedisConstants.LOGIN_USER_KEY;
import static com.sean.comment.utils.RedisConstants.LOGIN_USER_TTL;
import static com.sean.comment.utils.SystemConstants.LOGIN_USER;

public class LoginInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 session
        // final HttpSession session = request.getSession();
        // 从 session 中获取用户
        // final User user = (User)session.getAttribute(LOGIN_USER);

        // 获取请求头中的 token
        final String token = request.getHeader("authorization");
        if(StringUtils.isBlank(token)){
            response.setStatus(401); // 未授权
            return false;
        }
        // 查询 redis 中的用户
        final Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
        // 判断用户是否存在
        if(userMap.isEmpty()){
            response.setStatus(401); // 未授权
            return false;
        }
        // 解析用户
        final UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 保存用户到 ThreadLocal 中
//        UserDTO userDTO = new UserDTO();
//        userDTO.setId(user.getId());
//        userDTO.setNickName(user.getNickName());
//        userDTO.setIcon(user.getIcon());
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
