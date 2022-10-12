package com.sean.comment.controller.interceptor;


import com.sean.comment.dto.UserDTO;
import com.sean.comment.entity.User;
import com.sean.comment.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import static com.sean.comment.utils.SystemConstants.LOGIN_USER;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 session
        final HttpSession session = request.getSession();
        // 从 session 中获取用户
        final User user = (User)session.getAttribute(LOGIN_USER);
        // 判断用户是否存在
        if(user == null){
            response.setStatus(401); // 未授权
            return false;
        }
        // 保存用户到 ThreadLocal 中
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setNickName(user.getNickName());
        userDTO.setIcon(user.getIcon());
        UserHolder.saveUser(userDTO);
        // 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
