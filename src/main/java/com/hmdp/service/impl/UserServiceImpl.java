package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huy
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1. 校验手机号
//        if(!RegexUtils.isPhoneInvalid(phone)){
//            return Result.fail("手机号格式错误");
//        }
        //2. 生成验证码
        String code = RandomUtil.randomString(6);
        //3.保存验证码--->redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code);
        //4.发送验证码
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        if(loginForm==null){
            return Result.fail("输入信息为空");
        }
        //校验手机号
        String phone = loginForm.getPhone();
        if(phone.isEmpty()){
            return Result.fail("手机号格式错误");
        }
        String relCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if(relCode.isEmpty()||!relCode.equals(loginForm.getCode())){
            //验证码不存在或者输入的验证码错误
            return Result.fail("验证码不存在或者输入的验证码错误");
        }
        //登录成功，保存用户信息到redis
        User user = this.query().eq("phone", phone).one();

        if(user==null){
            //不存在，注册新用户
             user=createUserWithPhone( phone);
            this.save(user);
        }
        UserDTO userDTO=new UserDTO();
        BeanUtil.copyProperties(user,userDTO);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((filedName, filedValue) -> filedValue.toString()));
        String token = UUID.randomUUID().toString();
        String tokenKey=LOGIN_USER_KEY+token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL, TimeUnit.SECONDS);

        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        User user=new User();
        user.setPhone(phone);
        user.setNickName("user_"+RandomUtil.randomString(10));
        return user;
    }
}
