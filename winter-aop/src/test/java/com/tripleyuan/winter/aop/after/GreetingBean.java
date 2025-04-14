package com.tripleyuan.winter.aop.after;

import com.tripleyuan.winter.annotation.Around;
import com.tripleyuan.winter.annotation.Component;

@Component
@Around("politeInvocationHandler")
public class GreetingBean {

    public String hello(String name) {
        return "Hello, " + name + ".";
    }

    public String morning(String name) {
        return "Morning, " + name + ".";
    }
}
