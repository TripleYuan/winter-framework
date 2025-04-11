package com.tripleyuan.winter.exception;

public class BeanCreationException extends RuntimeException {

    public BeanCreationException(String message) {
        super(message);
    }

    public BeanCreationException(Throwable cause) {
        super(cause);
    }

}
