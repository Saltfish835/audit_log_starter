package com.example.auditstarter.handler;


import com.example.auditstarter.bean.AuditLog;

/**
 * handler基类
 */
public abstract class AuditHandler {

    /**
     * 记录下一个handler
     */
    protected AuditHandler auditHandler;

    /**
     * 设置下一个handler
     * @param handler
     */
    public void setNext(AuditHandler handler) {
        this.auditHandler = handler;
    }

    /**
     * 具体的业务处理
     * @param auditLog
     */
    public abstract void processor(AuditLog auditLog);
}
