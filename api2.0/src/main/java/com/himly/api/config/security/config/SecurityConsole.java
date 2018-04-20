package com.himly.api.config.security.config;

/**
 * create_at:MaZheng
 * create_by:${date} ${time}
 */
public class SecurityConsole {

    public static final String SECRET = "SecretKeyToGenJWTs";

    //10 day
    public static final long EXPIRATION_TIME = 60*5*1000;

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String HEADER_STRING = "Authorization";

    public static final String SING_UP_URL = "/a/users/sign-up";

    public static final boolean POST_ONLY = true;
}
