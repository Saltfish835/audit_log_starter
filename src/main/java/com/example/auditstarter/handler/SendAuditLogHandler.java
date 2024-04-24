package com.example.auditstarter.handler;

import com.example.auditstarter.bean.AuditLog;
import com.example.auditstarter.common.utils.RequestUtil;
import com.example.auditstarter.config.AuditLogProperties;
import com.google.gson.Gson;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


/**
 * 保存审计日志的handler
 */
public class SendAuditLogHandler extends AuditHandler{

    private final String url;

    private final AsyncHttpClient asyncHttpClient;

    private final ExecutorService responseHandleThreadPool;

    public SendAuditLogHandler(AuditLogProperties properties, AsyncHttpClient asyncHttpClient, ExecutorService responseHandleThreadPool) {
        this.url = properties.getServiceUrl();
        this.asyncHttpClient = asyncHttpClient;
        this.responseHandleThreadPool = responseHandleThreadPool;
    }

    @Override
    public void processor(AuditLog auditLog) {
        // 组装请求对象
        Map<String, Object> params = new HashMap<>();
        params.put("srcIP", auditLog.getIp());
        params.put("username", auditLog.getUsername());
        params.put("content", auditLog.getMsg());
        params.put("contentEn", auditLog.getMsgEn());
        String jsonContent = new Gson().toJson(params);
        Request request = RequestUtil.getRequest(url, jsonContent);
        // 异步发送请求
        ListenableFuture<Response> whenResponse  = asyncHttpClient.executeRequest(request);
        // 异步处理响应结果
        whenResponse.addListener(()->{
            try{
                Response response = whenResponse.get();
                int code = response.getStatusCode();
                if(code == 200) {
                    System.out.println("send audit log successfully");
                } else {
                    System.out.println("send audit log error");
                }
            }catch (Exception e) {
                System.out.println("send audit log error");
            }
        }, responseHandleThreadPool);
    }
}
