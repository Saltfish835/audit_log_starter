package com.example.auditstarter.config;

import com.example.auditstarter.annotation.AuditLog;
import com.example.auditstarter.common.utils.RemotingUtil;
import com.example.auditstarter.interceptor.AuditLogAnnotationInterceptor;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 自动配置类
 */
@Configuration
@EnableConfigurationProperties(AuditLogProperties.class)
@ConditionalOnProperty(prefix = "audit.log", name="serviceUrl") // 只有在配置文件中配置了audit.log.serviceUrl才启用审计日志记录功能
public class AuditLogAutoConfiguration implements DisposableBean {

    @Autowired
    private AuditLogProperties auditLogProperties;

    private EventLoopGroup eventLoopGroupBoss;

    private EventLoopGroup eventLoopGroupWorker;

    private AsyncHttpClient asyncHttpClient;

    private ExecutorService responseHandleThreadPool;

    @Bean
    public Advisor auditLogAnnotationAdvisor(BeanFactory beanFactory) {
        // 初始化所需参数
        initEventLoopGroupBoss();
        initEventLoopGroupWorker();
        initAsyncHttpClient();
        initResponseHandleThreadPool();
        // 拦截所有被AuditLog注解修饰的方法
        AuditLogAnnotationInterceptor interceptor = new AuditLogAnnotationInterceptor(beanFactory, auditLogProperties, asyncHttpClient, responseHandleThreadPool);
        AnnotationMatchingPointcut pointcut = AnnotationMatchingPointcut.forMethodAnnotation(AuditLog.class);
        return new DefaultPointcutAdvisor(pointcut,interceptor);
    }


    public void initEventLoopGroupBoss() {
        if(RemotingUtil.isLinuxPlatform() && Epoll.isAvailable()) { // 当前环境可以使用epoll模式
            this.eventLoopGroupBoss = new EpollEventLoopGroup(auditLogProperties.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("netty-boss-epoll"));
        }else {
            this.eventLoopGroupBoss = new NioEventLoopGroup(auditLogProperties.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("netty-boss-nio"));
        }
    }


    public void initEventLoopGroupWorker() {
        if(RemotingUtil.isLinuxPlatform() && Epoll.isAvailable()) { // 当前环境可以使用epoll模式
            this.eventLoopGroupWorker =  new EpollEventLoopGroup(auditLogProperties.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("netty-worker-epoll"));
        }else {
            this.eventLoopGroupWorker = new NioEventLoopGroup(auditLogProperties.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("netty-worker-nio"));
        }
    }


    public void initAsyncHttpClient() {
        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder()
                .setEventLoopGroup(eventLoopGroupWorker)
                .setConnectTimeout(auditLogProperties.getHttpConnectTimeout())
                .setRequestTimeout(auditLogProperties.getHttpRequestTimeout())
                .setMaxRequestRetry(auditLogProperties.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true)
                .setMaxConnections(auditLogProperties.getHttpMaxContentions())
                .setMaxConnectionsPerHost(auditLogProperties.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(auditLogProperties.getHttpPooledConnectionIdleTimeout());
        this.asyncHttpClient =  new DefaultAsyncHttpClient(builder.build());
    }


    public void initResponseHandleThreadPool() {
        this.responseHandleThreadPool = Executors.newFixedThreadPool(auditLogProperties.getResponseHandleThreadNum());
    }


    /**
     * 容器关闭时，释放资源
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        if(asyncHttpClient != null) {
            try {
                asyncHttpClient.close();
            }catch (IOException e) {

            }
        }
        if(eventLoopGroupBoss != null) {
            eventLoopGroupBoss.shutdownGracefully();
        }
        if(eventLoopGroupWorker != null) {
            eventLoopGroupWorker.shutdownGracefully();
        }
        if(responseHandleThreadPool != null) {
            responseHandleThreadPool.shutdown();
        }
    }
}
