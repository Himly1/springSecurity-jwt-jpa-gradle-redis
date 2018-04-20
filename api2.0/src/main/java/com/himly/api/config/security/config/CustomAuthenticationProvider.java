package com.himly.api.config.security.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *
 * @author himly z1399956473@gmail.com
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = Logger.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    BCryptPasswordEncoder bCrypt;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {


        if (null==authentication) {
            log.info("authentication is null");
            throw new AuthenticationServiceException("authentication can not be null");
        }


        String account = authentication.getName();
        log.info("account is=="+account);

        String pwd = (String) authentication.getCredentials();
        log.info("pwd is=="+pwd);

        UserDetails userDetails = userDetailsService.loadUserByUsername(account);
        log.info("userDetails is=="+userDetails);

        if (null==pwd) {

            log.info("pwd is null,ignore verify");
            return new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
        }else if (bCrypt.matches(pwd,userDetails.getPassword())){

            log.info("success verify");
            return new UsernamePasswordAuthenticationToken(userDetails,authentication.getCredentials(),userDetails.getAuthorities());
        }else {

            log.info("failed verify");
            throw new BadCredentialsException("account or password incorrect");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
