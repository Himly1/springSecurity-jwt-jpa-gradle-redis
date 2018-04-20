package com.himly.api.config.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.himly.api.model.User;
import com.himly.api.utils.JwtUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author himly z1399956473@gmail.com
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

    private static final Logger log = Logger.getLogger(JwtAuthenticationFilter.class);

    private AuthenticationManager authenticationManager;


    private static final String[] ILLEGAL_PASSWORD_LIST = {
            "null"
    };

    @Autowired
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (SecurityConsole.POST_ONLY&&!request.getMethod().equals("POST")) {
            log.info("request method is=="+request.getMethod());
            throw new AuthenticationServiceException(
                    "Authentication method not supported"
            );

        }else {
            try{
                User user = new ObjectMapper().readValue(
                        request.getInputStream(),
                        User.class
                );
                log.info("user is=="+user);

                if (null==user) {
                    log.info("user is null");
                    throw new AuthenticationServiceException("user info can not be null");
                }

                if (isPasswordIllegal(user.getPassword())) {
                    log.info("password illegal");
                    throw new AuthenticationServiceException("pwd illegal,pls change another one");
                }

                if (null==user.getName()) {
                    user.setName("");
                }

                if (null==user.getPassword()) {
                    user.setPassword("");
                }

                return authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getName(),
                                user.getPassword(),
                                new ArrayList<>()
                        )
                );
            }catch (IOException e) {
                log.error("has an IoException ,see=="+e.getMessage(),e);
                throw new RuntimeException(e);
            }catch (Throwable t) {
                log.error("has an error,see=="+t.getMessage(),t);
                throw t;
            }
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        log.info("authentication is=="+authResult);
        
        try {
            String token = JwtUtil.createToken(authResult);
            response.addHeader(SecurityConsole.HEADER_STRING,token);
        }catch (Throwable t) {
            log.error("has an error,see=="+t.getMessage(),t);
        }
    }


    public static boolean isPasswordIllegal(String pwd) {

        log.info("pwd is=="+pwd);

        if (null==pwd) {
            return false;
        }


        for (String st: ILLEGAL_PASSWORD_LIST) {
            if (st.equals(pwd)) {
                return true;
            }
        }

        return false;
    }
}
