package com.himly.api.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author himly z1399956473@gmail.com
 */
public class HttpReqUtil {

    private static final Logger log = Logger.getLogger(HttpReqUtil.class);

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static HttpEntity sendGetRequest(String url, Header[] headers) throws Exception{

        log.info("url is=="+url+"   headers is=="+headers);

        if (null==url||url.isEmpty()) {

            log.info("url is illegal");
            throw new Exception("url is illegal,can not be null or empty");
        }


        HttpGet httpGet = new HttpGet(url);

        if (null!=headers&&0!=headers.length) {
            httpGet.setHeaders(headers);
        }

        CloseableHttpResponse response = httpClient.execute(httpGet);

        try{
            log.info("response is=="+response.getStatusLine());

            HttpEntity httpEntity = response.getEntity();

            log.info("httpEntity is=="+httpEntity);

            return httpEntity;
        }catch (Throwable t) {
            log.error("has an error,see=="+t.getMessage(),t);
            throw t;
        }
    }
}
