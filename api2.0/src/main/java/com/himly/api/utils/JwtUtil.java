package com.himly.api.utils;

import com.himly.api.config.security.config.SecurityConsole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.bytebuddy.asm.Advice;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 *
 * @author himly z1399956473@gmail.com
 */
@Component
public class JwtUtil {

    private static final Logger log = Logger.getLogger(JwtUtil.class);
    private static final String SPLIT_SYMBOL = ",";
    private static final int SUBJECT_ARGS_SIZE  = 2;
    public static final String BLACK_LIST_PREFIX = "BLACK_LIST:";

    private static  RedisTemplate<String,String> redisTemplate;
    private static ValueOperations<String,String> valueOperations;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        JwtUtil.redisTemplate = redisTemplate;
        valueOperations = redisTemplate.opsForValue();
    }

    public static String createToken(Authentication authResult) throws Exception{

        log.info("authResult is=="+authResult);

        if (null==authResult) {
            log.info("authResult is null");
            throw new Exception("authResult can not be null");
        }

        String username = authResult.getName();
        String pwd;
        Object credentials = authResult.getCredentials();

        if (null==credentials) {
            pwd = null;
        }else {
            pwd = (String) credentials;
        }


        //TODO subject中存放用户唯一的属性,例如手机号及id等,需要重写userDetails来实现
        String token = SecurityConsole.TOKEN_PREFIX+Jwts.builder()
                .setSubject(
                        username
                        +SPLIT_SYMBOL
                        +pwd
                )
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()+SecurityConsole.EXPIRATION_TIME
                        )
                )
                .signWith(
                        SignatureAlgorithm.HS256,
                        SecurityConsole.SECRET
                ).compact();

        log.info("token is=="+token);

        return token;
    }


    public static UsernamePasswordAuthenticationToken getAuthentication(String token) throws Exception{

        log.info("token is=="+token);

        if (null==token) {
            log.info("token is null");
            throw new Exception("token can not be null");
        }

        Claims claims = Jwts.parser().setSigningKey(SecurityConsole.SECRET)
                .parseClaimsJws(token.replace(SecurityConsole.TOKEN_PREFIX,""))
                .getBody();

        String subject = claims.getSubject();
        log.info("subject is =="+subject);

        if (null==subject) {
            log.info("subject is null");
            throw new Exception("bad token,subject can not be null");
        }

        String[] args = subject.split(SPLIT_SYMBOL);
        if (SUBJECT_ARGS_SIZE!=args.length) {
            log.info("subject args size is illegal");
            throw new Exception("bad token,subject args size is illegal");
        }

        String username = args[0];
        log.info("username is=="+username);

        String pwd = args[1];
        log.info("pwd is=="+pwd);
        if ("null".equals(pwd)) {
            log.info("backstage login,set pwd null");
            pwd = null;
        }

        Date expiration = claims.getExpiration();
        Date now = new Date();

        if (now.getTime() > expiration.getTime()) {
            throw new Exception("token timeout");
        }

        if (null==username) {
            log.info("username is null");
            throw new Exception("bad token,username can not be null");
        }

        return new UsernamePasswordAuthenticationToken(username,pwd);
    }

    public static String getToken(HttpServletRequest request) throws Exception{

        log.info("request is=="+request);

        if (null==request) {
            log.info("request is null");
            throw new Exception("request can not be null");
        }

        String token = request.getHeader(SecurityConsole.HEADER_STRING);
        log.info("token is=="+token);

        return token;
    }

    public static String getUserAccount(String token) throws Exception{

        log.info("token is=="+token);

        Claims claims = Jwts.parser().setSigningKey(SecurityConsole.SECRET)
                .parseClaimsJws(token.replace(SecurityConsole.TOKEN_PREFIX,""))
                .getBody();

        String subject = claims.getSubject();
        if (null==subject) {
            log.info("subject is null");
            throw new Exception("bad token,subject can not be null");
        }

        String[] args = subject.split(SPLIT_SYMBOL);
        if (SUBJECT_ARGS_SIZE!=args.length) {
            log.info("subject args size is illegal");
            throw new Exception("bad token,subject args size is illegal");
        }

        String account = args[0];
        log.info("account is=="+account);

        return account;
    }

    public static Long getResidueTime(String token) {

        log.info("token is=="+token);

        Claims claims = Jwts.parser().setSigningKey(SecurityConsole.SECRET)
                .parseClaimsJws(token.replace(SecurityConsole.TOKEN_PREFIX,""))
                .getBody();


        Date expiration = claims.getExpiration();
        Date now = new Date();

        Long residueTime = expiration.getTime()-now.getTime();
        log.info("residue time is=="+residueTime);

        return residueTime;
    }

    public static boolean isIllegal(String token) {

        log.info("token is=="+token);

        return valueOperations.get(BLACK_LIST_PREFIX+token)!=null;
    }
}
