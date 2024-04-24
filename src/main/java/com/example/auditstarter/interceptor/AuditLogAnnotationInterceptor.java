package com.example.auditstarter.interceptor;

import com.example.auditstarter.bean.AuditLog;
import com.example.auditstarter.config.AuditLogProperties;
import com.example.auditstarter.handler.AnnotationInfoHandler;
import com.example.auditstarter.handler.ExceptionPostHandler;
import com.example.auditstarter.handler.SendAuditLogHandler;
import com.example.auditstarter.handler.SuccessPostHandler;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.asynchttpclient.AsyncHttpClient;
import org.springframework.beans.factory.BeanFactory;

import java.util.concurrent.ExecutorService;


public class AuditLogAnnotationInterceptor implements MethodInterceptor {

    private final BeanFactory beanFactory;
    private final AuditLogProperties properties;
    private final AsyncHttpClient asyncHttpClient;
    private final ExecutorService responseHandleThreadPool;

    public AuditLogAnnotationInterceptor(BeanFactory beanFactory, AuditLogProperties properties, AsyncHttpClient asyncHttpClient, ExecutorService responseHandleThreadPool) {
        this.beanFactory = beanFactory;
        this.properties = properties;
        this.asyncHttpClient = asyncHttpClient;
        this.responseHandleThreadPool = responseHandleThreadPool;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        AuditLog auditLog = new AuditLog();
        AnnotationInfoHandler annotationInfoHandler = new AnnotationInfoHandler(invocation, beanFactory);
        SendAuditLogHandler sendAuditLogHandler = new SendAuditLogHandler(properties, asyncHttpClient, responseHandleThreadPool);
        try{
            // 调用被代理类的方法
            Object result = invocation.proceed();
            SuccessPostHandler successPostHandler = new SuccessPostHandler(result);
            // 成功调用时设置过滤器链
            annotationInfoHandler.setNext(successPostHandler);
            successPostHandler.setNext(sendAuditLogHandler);
            return result;
        }catch (Throwable t) {
            // 失败调用时设置过滤器链
            ExceptionPostHandler exceptionPostHandler = new ExceptionPostHandler(t);
            annotationInfoHandler.setNext(exceptionPostHandler);
            exceptionPostHandler.setNext(sendAuditLogHandler);
            throw t;
        }finally {
            // 启动过滤器链
            annotationInfoHandler.processor(auditLog);
        }
    }
}
