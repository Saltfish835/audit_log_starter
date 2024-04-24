package com.example.auditstarter.handler;

import com.example.auditstarter.bean.AuditLog;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


/**
 * 获取方法执行时的信息
 */
public abstract class MethodPostHandler extends AuditHandler{


    protected Object result;

    public MethodPostHandler(Object result) {
        this.result = result;
    }

    @Override
    public void processor(AuditLog auditLog) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if(servletRequestAttributes == null) {
            throw new RuntimeException("request info is null");
        }
        // 设置登录信息
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String userName = (String)request.getSession().getAttribute("userName");
        if (userName != null) {
            auditLog.setUsername(userName);
        }
        String remoteHost = request.getRemoteHost();
        auditLog.setIp(remoteHost);
        auditLog.setTime(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
        // 设置方法调用结果信息
        setMethodResult(auditLog);
        // 交由下一个handler进行处理
        auditHandler.processor(auditLog);
    }

    /**
     * 模板方法，处理方法调用成功与调用失败不同的情况
     * @param auditLog
     */
    protected abstract void setMethodResult(AuditLog auditLog);

    protected enum R {
        SUCCESS("SUCCESS", "操作成功", "operation successful"), ERROR("ERROR", "操作失败", "operation failed"),
        EXCEPTION("EXCEPTION", "请求发生异常", "request has encountered an exception");

        R(String code, String msg, String msgEn) {
            this.code = code;
            this.msg = msg;
            this.msgEn = msgEn;
        }

        private final String code;
        private final String msg;
        private final String msgEn;

        public String getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        public String getMsgEn() {
            return msgEn;
        }
    }
}
