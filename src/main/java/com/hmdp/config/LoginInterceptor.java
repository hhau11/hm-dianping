package com.hmdp.config;

/*
 * @author blue
 *  @version 1.0
 */

import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {

   /**
    * 前置拦截器
    * @param request
    * @param response
    * @param handler
    * @return
    * @throws Exception
    */
   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      UserDTO user = UserHolder.getUser();
      if(user==null){
         response.setStatus(401);
         return false;
      }
      return true;
   }

   @Override
   public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
      HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
   }
}
