package com.tripleyuan.winter.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ComponentScan {

    String value() default "";

}
