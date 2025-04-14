package com.tripleyuan.winter.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Around {

    /**
     * Invocation Handler bean name.
     */
    String value();

}
