package com.tripleyuan.winter.aop.around;

import com.tripleyuan.winter.annotation.Around;
import com.tripleyuan.winter.annotation.Component;
import com.tripleyuan.winter.annotation.Value;

@Component
@Around("aroundInvocationHandler")
public class OriginBean {

    @Value("${customer.name}")
    public String name;

    @Polite
    public String hello() {
        return "Hello, " + name + ".";
    }

    public String morning() {
        return "Morning, " + name + ".";
    }
}
