package com.example.auditstarter.common.utils;

import com.example.auditstarter.common.constans.BasicConstants;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

public class RequestUtil {

    /**
     * 获取异步http请求对象
     * @param url
     * @param jsonContent
     * @return
     */
    public static Request getRequest(String url, String jsonContent) {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setMethod(BasicConstants.POST);
        requestBuilder.setUrl(url);
        requestBuilder.setHeader(BasicConstants.CONTENT_TYPE, BasicConstants.JSON_CONTENT_TYPE);
        requestBuilder.setBody(jsonContent);
        return requestBuilder.build();
    }

}
