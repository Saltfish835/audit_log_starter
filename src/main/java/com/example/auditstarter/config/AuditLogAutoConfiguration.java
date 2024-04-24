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


/**
 * 自动配置类
 */
@Configuration
@EnableConfigurationProperties(AuditLogProperties.class)
public class AuditLogAutoConfiguration implements DisposableBean {

    @Autowired
    private AuditLogProperties auditLogProperties;

    @Qualifier("eventLoopGroupBoss")
    private EventLoopGroup eventLoopGroupBoss;

    @Qualifier("eventLoopGroupWorker")
    private EventLoopGroup eventLoopGroupWorker;

    @Autowired
    private AsyncHttpClient asyncHttpClient;

    @Bean
    @ConditionalOnProperty(prefix = "audit.log")
    public Advisor auditLogAnnotationAdvisor(BeanFactory beanFactory) {
        // 拦截所有被AuditLog注解修饰的方法
        AuditLogAnnotationInterceptor interceptor = new AuditLogAnnotationInterceptor(beanFactory, auditLogProperties, asyncHttpClient);
        AnnotationMatchingPointcut pointcut = AnnotationMatchingPointcut.forMethodAnnotation(AuditLog.class);
        return new DefaultPointcutAdvisor(pointcut,interceptor);
    }


    @Bean("eventLoopGroupBoss")
    public EventLoopGroup initEventLoopGroupBoss() {
        if(RemotingUtil.isLinuxPlatform() && Epoll.isAvailable()) { // 当前环境可以使用epoll模式
            return new EpollEventLoopGroup(auditLogProperties.getEventLoopGroupBossNum(), new DefaultThreadFactory("netty-boss-epoll"));
        }else {
            return new NioEventLoopGroup(auditLogProperties.getEventLoopGroupBossNum(), new DefaultThreadFactory("netty-boss-nio"));
        }
    }


    @Bean("initEventLoopGroupWorker")
    public EventLoopGroup initEventLoopGroupWorker() {
        if(RemotingUtil.isLinuxPlatform() && Epoll.isAvailable()) {
            return new EpollEventLoopGroup(auditLogProperties.getEventLoopGroupWorkerNum(), new DefaultThreadFactory("netty-worker-epoll"));
        }else {
            return new NioEventLoopGroup(auditLogProperties.getEventLoopGroupWorkerNum(), new DefaultThreadFactory("netty-worker-nio"));
        }
    }


    @Bean
    public AsyncHttpClient initAsyncHttpClient() {
        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder().setEventLoopGroup(eventLoopGroupWorker)
                .setConnectTimeout(auditLogProperties.getHttpConnectTimeout())
                .setRequestTimeout(auditLogProperties.getHttpRequestTimeout())
                .setMaxRequestRetry(auditLogProperties.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true)
                .setMaxConnections(auditLogProperties.getHttpMaxContentions())
                .setMaxConnectionsPerHost(auditLogProperties.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(auditLogProperties.getHttpPooledConnectionIdleTimeout());
        return new DefaultAsyncHttpClient(builder.build());
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
    }
}
