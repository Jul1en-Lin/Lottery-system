package com.julien.lotterysystem.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class JwtUtil {
    //7天过期时间
    public static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    //生成Key
    public static final String SECRET = "+JEq/o2bYDetaECkxKCeAtOi3yA0IPUlV2rxFVGS+v4=";
    public static final Key KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

    /**
     * 生成token
     * @param claim 自定义内容(设置载荷)
     * @return
     */
    public static String genJwt(Map<String , Object> claim){
        return Jwts.builder()
                    .setClaims(claim) //自定义内容(设置载荷)
                    .setIssuedAt(new Date()) //设置签发时间
                    .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
                    .signWith(KEY)
                    .compact();
    }

    /**
     * 解析token
     * @param token
     * @return
     */
    public static Claims parseJWT(String token){
        if (!StringUtils.hasLength(token)){
            return null;
        }
        //创建解析器, 设置签名
        JwtParser build = Jwts.parserBuilder().setSigningKey(KEY).build();
        Claims body = null;
        try {
            body = build.parseClaimsJws(token).getBody();
        }catch (Exception e){
            log.error("token 不合法, token:{}", token);
        }
        return body;
    }

    /**
     * 从token中获取用户id
     * @param token
     * @return
     */
    public static Integer getUserIdFromToken(String token){
        Claims claims = parseJWT(token);
        if(claims == null){
            return null;
        }
        HashMap<String,Object> userInfo = new HashMap<>(claims);
        return (Integer) userInfo.get("userId");
    }
}

