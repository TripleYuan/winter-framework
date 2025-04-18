package com.tripleyuan.winter.annotation;

import java.lang.annotation.*;

import static com.tripleyuan.winter.utils.WebUtils.DEFAULT_PARAM_VALUE;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {

    String value();

    String defaultValue() default DEFAULT_PARAM_VALUE;

}
