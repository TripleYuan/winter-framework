package com.tripleyuan.winter.demo.bean;

import com.tripleyuan.winter.annotation.Autowired;
import com.tripleyuan.winter.annotation.Component;
import lombok.Getter;

@Component
public class CyclicBean2 {

    @Getter
    private CyclicBean1 cyclicBean1;

    @Autowired
    public void setCyclicBean1(CyclicBean1 cyclicBean1) {
        this.cyclicBean1 = cyclicBean1;
    }

}
