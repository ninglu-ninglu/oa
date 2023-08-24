package com.atguigu.security.filter;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.ResponseUtil;
import com.atguigu.common.result.Result;
import com.atguigu.common.result.ResultCodeEnum;
import com.atguigu.security.custom.LoginUserInfoHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
@time 2023/8/22-16:29
@authon cheny
@name 哈哈
@version 1.0
*/

public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private RedisTemplate redisTemplate;

    public TokenAuthenticationFilter(RedisTemplate redisTemplate) {
        this.redisTemplate=redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        logger.info("uri:"+request.getRequestURI());
        //如果是登录接口，直接放行
        if("/admin/system/index/login".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if(null != authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } else {
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.LOGIN_ERROR));
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        //判断请求头里面是否有token
        String token = request.getHeader("token");
        if (!StringUtils.isEmpty(token)){
            String username = JwtHelper.getUsername(token);
            if (!StringUtils.isEmpty(username)){
                //把当前用户信息放到Threadlocal
                LoginUserInfoHelper.setUserId(JwtHelper.getUserId(token));
                LoginUserInfoHelper.setUsername(JwtHelper.getUsername(token));
                //通过用户名称获取权限数据，从redis
                String authString = (String)redisTemplate.opsForValue().get(username);
                //把redis的字符串类型转换为集合类型
                if (!StringUtils.isEmpty(authString)){
                    List<Map> maps = JSON.parseArray(authString, Map.class);
                    List<SimpleGrantedAuthority> authList=new ArrayList<>();
                    for (Map m:maps) {
                        String authority = (String) m.get("authority");
                        authList.add(new SimpleGrantedAuthority(authority));
                    }
                    return new UsernamePasswordAuthenticationToken(username,null, authList);
                }else {
                    return new UsernamePasswordAuthenticationToken(username,null, new ArrayList<>());
                }

            }
        }
        return null;
    }
}
