package com.himly.api.config.security.config;

import com.himly.api.utils.JwtUtil;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author himly z1399956473@gmail.com
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter{

    private static final Logger log = Logger.getLogger(JwtAuthorizationFilter.class);

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader(SecurityConsole.HEADER_STRING);
        log.info("header is=="+header);

        if (null==header||!header.startsWith(SecurityConsole.TOKEN_PREFIX)) {
            log.info("header illegal");
            chain.doFilter(request,response);
            return;
        }

        if (JwtUtil.isIllegal(header)) {
            log.info("header is illegal");
            chain.doFilter(request,response);
            return;
        }

        try {
            UsernamePasswordAuthenticationToken authenticationToken = JwtUtil.getAuthentication(header);
            log.info("authentication token is=="+authenticationToken);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request,response);
        }catch (Throwable t) {
            log.error("has an error,see=="+t.getMessage(),t);
            SecurityContextHolder.clearContext();
        }
    }
}
