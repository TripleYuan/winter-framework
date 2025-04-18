package com.tripleyuan.winter.exception;

public class BeanCreationException extends BeansException {

    public BeanCreationException(String message) {
        super(message);
    }

    public BeanCreationException(Throwable cause) {
        super(cause);
    }

    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
