package com.himly.api.utils;

import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * @author himly z1399956473@gmail.com
 */
public class CodeMapper {

    private static final Logger log = Logger.getLogger(CodeMapper.class);

    private static final HashMap<Integer,String> CODE_MAPPER =  new HashMap<Integer,String>(){
        {
            /*######common##########*/
            /*######-1000-1000######*/
            put(0, "success");
            put(-1000, "server error");


            /*######用户模块##########*/
            /*######-2000-2000######*/
            put(-2000, "params name error");
            put(-2001, "password is illegal,pls change another one");
            put(-2002, "username can not be empty");
            put(-2003, "pwd can not be empty");
            put(-2004, "account or pwd error,check it!");
            put(-2005, "role not found");
            put(-2006, "account already existent,pls swap another one");
            put(-2007, "user not found");
            put(-2008, "account can not be empty");


            /*########cas-server######*/
            /*#######-3000-3000#######*/
            put(-3000, "cas server id illegal");
            put(-3001, "cas server not found");
            put(-3002, "got cas server,but no cas server location");
            put(-3003, "cas ticket is illegal");
            put(-3004, "verify ticket failed");
        }
    };


    public static String codeToMsg(Integer code) {

        log.info("code is=="+code);

        String msg = CODE_MAPPER.get(code);

        log.info("msg is=="+msg);

        return msg;
    }
}
