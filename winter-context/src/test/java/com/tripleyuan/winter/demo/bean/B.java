package com.tripleyuan.winter.demo.bean;

import com.tripleyuan.winter.annotation.Autowired;
import com.tripleyuan.winter.annotation.Component;
import lombok.Getter;

@Component
@Getter
public class B {

    private C c;

    public B(@Autowired C c) {
        this.c = c;
    }
}
