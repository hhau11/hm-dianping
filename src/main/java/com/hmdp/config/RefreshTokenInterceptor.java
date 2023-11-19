package com.hmdp.config;

/*
 * @author blue
 *  @version 1.0
 */

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.image.TileObserver;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;

public class RefreshTokenInterceptor implements HandlerInterceptor {
   private StringRedisTemplate stringRedisTemplate;
   public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
      this.stringRedisTemplate=stringRedisTemplate;
   }

   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      String token = request.getHeader("authorization");
      if(token==null||token.isEmpty()){
         return true;
      }
      Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
      if(userMap.isEmpty()){
         //用户不存在
         return true;
      }
      //用户存在，保存到ThreadLocal
      UserDTO userDTO= BeanUtil.fillBeanWithMap(userMap,new UserDTO(),false);
      UserHolder.saveUser(userDTO);
      //刷新token到期时间
      stringRedisTemplate.expire(LOGIN_USER_KEY+ token,30, TimeUnit.SECONDS);
      //放行

      return true;
   }
}
