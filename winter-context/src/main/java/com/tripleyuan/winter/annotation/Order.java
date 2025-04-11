package com.tripleyuan.winter.annotation;

import java.lang.annotation.*;

/**
 * Order is used to sort beans in the container. The value of Order more smaller, the higher the priority.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Order {

    int LOWEST_ORDER = Integer.MAX_VALUE;
    int HIGHEST_ORDER = Integer.MIN_VALUE;

    int value() default 0;

}
