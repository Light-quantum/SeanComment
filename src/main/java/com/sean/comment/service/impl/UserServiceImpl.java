package com.sean.comment.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.comment.dto.Result;
import com.sean.comment.entity.User;
import com.sean.comment.mapper.UserMapper;
import com.sean.comment.service.IUserService;
import com.sean.comment.utils.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 校验手机号 （通过自定义的正则表达式校验）
        if(RegexUtils.isPhoneInvalid(phone)){
            // 不符合返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 生成验证码
        final String code = RandomUtil.randomNumbers(6);
        // 保存验证码到 Session
        session.setAttribute("code", code);
        // 发送验证码
        // TODO 调用第三方短信服务 API 发送验证码
        log.debug("验证码发送成功，验证码：" + code);
        // 返回 ok
        return Result.ok();
    }
}
