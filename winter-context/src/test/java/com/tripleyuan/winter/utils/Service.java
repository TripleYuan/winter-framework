package com.tripleyuan.winter.utils;

import com.tripleyuan.winter.annotation.Component;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Component
public @interface Service {

    String value() default "";

}
