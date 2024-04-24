package com.example.auditstarter.handler;

import com.example.auditstarter.bean.AuditLog;
import com.example.auditstarter.common.constans.BasicConstants;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 获取注解中信息的handler
 */
public class AnnotationInfoHandler extends AuditHandler{


    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private MethodInvocation methodInvocation;
    private BeanFactory beanFactory;

    public AnnotationInfoHandler(MethodInvocation methodInvocation, BeanFactory beanFactory) {
        this.methodInvocation = methodInvocation;
        this.beanFactory = beanFactory;
    }

    @Override
    public void processor(AuditLog auditLog) {
        Method method = this.methodInvocation.getMethod();
        com.example.auditstarter.annotation.AuditLog annotation = AnnotationUtils.getAnnotation(method, com.example.auditstarter.annotation.AuditLog.class);
        if(annotation == null) {
            throw new RuntimeException("auditLog Annotation is null");
        }
        // 获取注解中的信息
        String[] rec = annotation.rec();
        String msg = annotation.msg();
        String msgEn = annotation.msgEn();
        Object[] arguments = methodInvocation.getArguments();
        ExpressionRootObject rootObject = new ExpressionRootObject(method, arguments, methodInvocation.getThis());
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, arguments, NAME_DISCOVERER);
        context.setBeanResolver(new BeanFactoryResolver(this.beanFactory));
        String elValues = Arrays.stream(rec).map(item -> {
            if (item.startsWith(BasicConstants.EL_PREFIX)) {
                return PARSER.parseExpression(item).getValue(context);
            } else {
                return item;
            }
        }).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(BasicConstants.JOIN_SEP));
        msg = Arrays.stream(ArrayUtils.toArray(msg, elValues)).filter(StringUtils::isNotEmpty).collect(Collectors.joining(BasicConstants.JOIN_SEP));
        msgEn = Arrays.stream(ArrayUtils.toArray(msgEn, elValues)).filter(StringUtils::isNotEmpty).collect(Collectors.joining(BasicConstants.JOIN_SEP));
        //记录审计日志
        auditLog.setMsg(msg);
        auditLog.setMsgEn(msgEn);
        // 交由下一个handler进行处理
        auditHandler.processor(auditLog);
    }

    public static class ExpressionRootObject {
        private final Method method;
        private final Object[] args;
        private final Object target;

        public ExpressionRootObject(Method method, Object[] args, Object target) {
            this.method = method;
            this.args = args;
            this.target = target;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getArgs() {
            return args;
        }

        public Object getTarget() {
            return target;
        }
    }
}
