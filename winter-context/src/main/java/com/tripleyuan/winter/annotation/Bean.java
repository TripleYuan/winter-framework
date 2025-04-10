package com.tripleyuan.winter.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Bean {

    /**
     * Bean name. Default to simple class name with first-letter-lower-case.
     */
    String value() default "";

    String initMethod() default "";

    String destroyMethod() default "";

}
