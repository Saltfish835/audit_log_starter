package com.example.auditstarter.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    String[] rec() default {};

    String msg() default "";

    String msgEn() default "";
}
