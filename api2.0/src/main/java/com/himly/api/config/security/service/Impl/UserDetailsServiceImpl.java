package com.himly.api.config.security.service.Impl;

import com.himly.api.model.User;
import com.himly.api.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * @author himly z1399956473@gmail.com
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService{

    private static final Logger log = Logger.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {

        log.info("account is=="+account);

        User user =  userRepository.findByAccount(account);
        log.info("user is=="+user);

        if (null==user) {
            log.info("user is null,user not found");
            throw new UsernameNotFoundException(account);
        }

        Set<GrantedAuthority>  authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getRole()));
        });

        return new org.springframework.security.core.userdetails.User(user.getAccount(),user.getPassword(),authorities);
    }
}
