package com.tripleyuan.winter.demo.bean;

import com.tripleyuan.winter.annotation.Autowired;
import com.tripleyuan.winter.annotation.Component;
import lombok.Getter;

@Component
public class CyclicBean1 {

    @Getter
    private CyclicBean2 cyclicBean2;

    @Autowired
    public void setCyclicBean2(CyclicBean2 cyclicBean2) {
        this.cyclicBean2 = cyclicBean2;
    }
}
