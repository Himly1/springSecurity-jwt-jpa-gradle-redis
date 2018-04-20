package com.himly.api.utils;

import org.apache.http.HttpEntity;
import org.apache.log4j.Logger;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author himly z1399956473@gmail.com
 */
public class CasResponseResolveUtil {

    public static final String USERNAME = "name";

    public static final String ACCOUNT = "account";

    public static final String STATUS = "status";

    public static final String MSG = "msg";

    public static final Logger log = Logger.getLogger(CasResponseResolveUtil.class);

    /**
     * resolve cas server callback request
     * @param entity
     * @return
     */
    public static Map<String,Object> resolveResponse(HttpEntity entity) throws Exception{

        log.info("entity is=="+entity);

        if (null==entity) {
            log.info("entity is null");
            throw new Exception("entity illegal,can not be null");
        }

        Map<String,Object> info = new HashMap<>(4);

        InputStreamReader inputStreamReader = new InputStreamReader(entity.getContent());

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;
        String result = "";

        while ( (line = bufferedReader.readLine())!=null ) {
            result += line;
        }
        log.info("result is=="+result);


        info.put(ACCOUNT,getAccount(result));
        info.put(USERNAME,getUsername(result));
        String msg = getStatus(result);

        if (msg.equals("success")) {
            info.put(STATUS,"success");
            info.put(MSG,"success");
        }else {
            info.put(STATUS,"failed");
            info.put(MSG,msg);
        }

        return info;
    }



    private static String getAccount(String result) {

        String regex = "<cas:user>(.*)</cas:user>";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(result);

        while (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }


    private static String getUsername(String result) {


        String regex = "<cas:ACPNAME>(.*)</cas:ACPNAME>";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(result);
        String name = "";

        while (matcher.find()) {
            name = matcher.group(1);
        }

        try {
            if (!name.isEmpty()) {
                byte[] bytes = Base64.getDecoder().decode(name);
                name = new String(bytes,"utf-8");
            }
        }catch (Throwable t) {
            log.error("has an error,see=="+t.getMessage(),t);
            return name;
        }

        return name;
    }


    private static String getStatus(String result) {

        String regex = "<cas:authenticationFailure (.*)</cas:authenticationFailure>";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(result);

        while (matcher.find()) {
            return matcher.group(1);
        }


        return "success";
    }
}
