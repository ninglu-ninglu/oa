package com.atguigu.common.jwt;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;
/*
@time 2023/8/21-21:40
@authon cheny
@name 哈哈
@version 1.0
*/
//jwt的工具类
public class JwtHelper {
    private static long tokenExpiration = 365 * 24 * 60 * 60 * 1000;
    private static String tokenSignKey = "123456";

    //用户id和用户名称
    public static String createToken(Long userId, String username) {
        String token = Jwts.builder()
                //分类
                .setSubject("AUTH-USER")
                //设置token有效时常
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                //主体部分
                .claim("userId", userId)
                .claim("username", username)
                //签名部分
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    //从token字符串中获取用户id
    public static Long getUserId(String token) {
        try {
            if (StringUtils.isEmpty(token)) return null;

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            Integer userId = (Integer) claims.get("userId");
            return userId.longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //从token字符串中获取用户名称
    public static String getUsername(String token) {
        try {
            if (StringUtils.isEmpty(token)) return "";

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return (String) claims.get("username");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String token = JwtHelper.createToken(7L, "zhangsan");
        System.out.println(token);
//        Long userId = JwtHelper.getUserId(token);
//        String username = JwtHelper.getUsername(token);
//        System.out.println(userId);
//        System.out.println(username);
    }
}
