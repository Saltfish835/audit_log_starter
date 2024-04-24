package com.example.auditstarter.handler;

import com.example.auditstarter.bean.AuditLog;
import com.example.auditstarter.common.constans.BasicConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 获取方法执行成功的返回信息
 */
public class SuccessPostHandler extends MethodPostHandler{

    public SuccessPostHandler(Object result) {
        super(result);
    }

    @Override
    protected void setMethodResult(AuditLog auditLog) {
        auditLog.setMsg(StringUtils.join(ArrayUtils.toArray(auditLog.getMsg(), R.SUCCESS.getMsg()), BasicConstants.JOIN_SEP));
        auditLog.setMsgEn(StringUtils.join(ArrayUtils.toArray(auditLog.getMsgEn(), R.SUCCESS.getMsgEn()), BasicConstants.JOIN_SEP));
    }
}
