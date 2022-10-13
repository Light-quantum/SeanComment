package com.sean.comment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.comment.dto.LoginFormDTO;
import com.sean.comment.dto.Result;
import com.sean.comment.dto.UserDTO;
import com.sean.comment.entity.User;
import com.sean.comment.mapper.UserMapper;
import com.sean.comment.service.IUserService;
import com.sean.comment.utils.RegexUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sean.comment.utils.RedisConstants.*;
import static com.sean.comment.utils.SystemConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@SuppressWarnings("all")
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 检测是否存在锁
        // if(stringRedisTemplate.hasKey(LOGIN_CODE_LOCK + phone)
        //         && stringRedisTemplate.getExpire(LOGIN_CODE_LOCK + phone) > 0){
        //     return Result.fail("获取验证码速度过快，请稍后重试");
        // }
        // 校验手机号 （通过自定义的正则表达式校验）
        if(RegexUtils.isPhoneInvalid(phone)){
            // 不符合返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 加锁防止同时获取验证码, login:code:phone = lock 有效期 1 min
        // stringRedisTemplate.opsForValue().set(LOGIN_CODE_LOCK + phone, "lock", LOGIN_CODE_LOCK_TTL, TimeUnit.MINUTES);
        // 生成验证码
        final String code = RandomUtil.randomNumbers(6);
        // 保存验证码到 Session
        // session.setAttribute(VERIFICATION_CODE, code);
        // 将验证码存到 redis 中. login:code:phone = code 有效期 5 min
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 发送验证码
        // TODO 调用第三方短信服务 API 发送验证码
        log.debug("验证码发送成功，验证码：" + code);
        // 返回 ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 校验手机号
        final String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            // 不符合返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 校验验证码
        // final String cacheCode = (String)session.getAttribute(VERIFICATION_CODE);
        // 从 redis 中获取验证码
        final String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        final String code = loginForm.getCode();
        if(StringUtils.isBlank(code) || !StringUtils.equals(code, cacheCode)){
            // 验证码不一致，返回错误信息
            return Result.fail("验证码错误");
        }
        // 根据手机号查询用户
        // 通过 mybatis plus 查询数据库
        User user = query().eq("phone", phone).one();
        if(user == null){
            // 用户不存在，快捷注册
            user = createUserWithPhone(phone);
        }
        // 保存用户到 session
        // session.setAttribute(LOGIN_USER, user);

        // 保存用户到 redis
        // 生成登录令牌
        String token = UUID.randomUUID().toString(true  );
        // User 转为 Hash 存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 将 UserDTO 全部转为 String - String
        final Map<String, Object> map = BeanUtil.beanToMap(user, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fielName, fielValue)-> fielValue.toString()));
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, map);
        // 设置有效期 30 分钟，但这里似乎有问题，set 和 设置有效期不是同时的
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 返回 ok
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        // 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(6));
        // 保存用户
        save(user);
        return user;
    }
}
