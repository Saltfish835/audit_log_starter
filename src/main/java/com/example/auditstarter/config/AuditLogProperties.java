package com.example.auditstarter.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 获取配置文件中audit.log开头的配置
 */
@ConfigurationProperties("audit.log")
public class AuditLogProperties {

    /**
     * Netty Boss线程数
     */
    private int eventLoopGroupBossNum = 1;

    /**
     * Netty Worker线程数
     */
    private int eventLoopGroupWorkerNum = 2;

    /**
     * 远程保存audit log地址
     */
    private String serviceUrl = "http://127.0.0.1:8089/base-module-web/innerService/audit/add";

    /**
     * 连接超时时间
     */
    private int httpConnectTimeout = 30 * 1000;

    /**
     * 请求超时时间
     */
    private int httpRequestTimeout = 30 * 1000;

    /**
     * 重试次数
     */
    private int httpMaxRequestRetry = 2;

    /**
     * 最大连接数
     */
    private int httpMaxContentions = 100;

    /**
     * 每个地址的最大连接数
     */
    private int httpConnectionsPerHost = 10;

    /**
     * 空闲连接超时时间
     */
    private int httpPooledConnectionIdleTimeout = 60 * 1000;

    /**
     * 处理响应的线程数
     */
    private int responseHandleThreadNum = 2;


    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public int getHttpConnectTimeout() {
        return httpConnectTimeout;
    }

    public void setHttpConnectTimeout(int httpConnectTimeout) {
        this.httpConnectTimeout = httpConnectTimeout;
    }

    public int getHttpRequestTimeout() {
        return httpRequestTimeout;
    }

    public void setHttpRequestTimeout(int httpRequestTimeout) {
        this.httpRequestTimeout = httpRequestTimeout;
    }

    public int getHttpMaxRequestRetry() {
        return httpMaxRequestRetry;
    }

    public void setHttpMaxRequestRetry(int httpMaxRequestRetry) {
        this.httpMaxRequestRetry = httpMaxRequestRetry;
    }

    public int getHttpMaxContentions() {
        return httpMaxContentions;
    }

    public void setHttpMaxContentions(int httpMaxContentions) {
        this.httpMaxContentions = httpMaxContentions;
    }

    public int getHttpConnectionsPerHost() {
        return httpConnectionsPerHost;
    }

    public void setHttpConnectionsPerHost(int httpConnectionsPerHost) {
        this.httpConnectionsPerHost = httpConnectionsPerHost;
    }

    public int getHttpPooledConnectionIdleTimeout() {
        return httpPooledConnectionIdleTimeout;
    }

    public void setHttpPooledConnectionIdleTimeout(int httpPooledConnectionIdleTimeout) {
        this.httpPooledConnectionIdleTimeout = httpPooledConnectionIdleTimeout;
    }

    public int getEventLoopGroupBossNum() {
        return eventLoopGroupBossNum;
    }

    public void setEventLoopGroupBossNum(int eventLoopGroupBossNum) {
        this.eventLoopGroupBossNum = eventLoopGroupBossNum;
    }

    public int getEventLoopGroupWorkerNum() {
        return eventLoopGroupWorkerNum;
    }

    public void setEventLoopGroupWorkerNum(int eventLoopGroupWorkerNum) {
        this.eventLoopGroupWorkerNum = eventLoopGroupWorkerNum;
    }

    public int getResponseHandleThreadNum() {
        return responseHandleThreadNum;
    }

    public void setResponseHandleThreadNum(int responseHandleThreadNum) {
        this.responseHandleThreadNum = responseHandleThreadNum;
    }
}