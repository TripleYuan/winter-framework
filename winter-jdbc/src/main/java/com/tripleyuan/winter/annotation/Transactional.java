package com.tripleyuan.winter.annotation;

import java.lang.annotation.*;

/**
 * Only class-level transactions are supported.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Transactional {

    String value() default "platformTransactionManager";

}
