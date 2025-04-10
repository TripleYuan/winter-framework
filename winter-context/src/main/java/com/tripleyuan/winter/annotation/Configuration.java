package com.tripleyuan.winter.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Component
public @interface Configuration {

    /**
     * Bean name. Default to simple class name with first-letter-lower-case.
     */
    String value();

}
