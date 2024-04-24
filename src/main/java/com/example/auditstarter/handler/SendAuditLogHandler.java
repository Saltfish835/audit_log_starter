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


/**
 * 保存审计日志的handler
 */
public class SendAuditLogHandler extends AuditHandler{

    private String url;

    private AsyncHttpClient asyncHttpClient;

    public SendAuditLogHandler(AuditLogProperties properties, AsyncHttpClient asyncHttpClient) {
        this.url = properties.getServiceUrl();
        this.asyncHttpClient = asyncHttpClient;
    }

    @Override
    public void processor(AuditLog auditLog) {
        Map<String, Object> params = new HashMap<>();
        params.put("srcIP", auditLog.getIp());
        params.put("username", auditLog.getUsername());
        params.put("content", auditLog.getMsg());
        params.put("contentEn", auditLog.getMsgEn());
        // 异步发送请求
        String jsonContent = new Gson().toJson(params);
        Request request = RequestUtil.getRequest(url, jsonContent);
        ListenableFuture<Response> future = asyncHttpClient.executeRequest(request);
        CompletableFuture<Response> completableFuture = future.toCompletableFuture();
        // 异步响应结果
        completableFuture.whenCompleteAsync(((response, throwable) -> {

        }));
    }
}
