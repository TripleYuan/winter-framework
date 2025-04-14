package com.tripleyuan.winter.demo.bean;

import com.tripleyuan.winter.annotation.Autowired;
import com.tripleyuan.winter.annotation.Component;
import lombok.Getter;

@Component
@Getter
public class A {

    private B b;

    public A(@Autowired B b) {
        this.b = b;
    }

}
