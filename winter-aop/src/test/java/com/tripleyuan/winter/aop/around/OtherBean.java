package com.tripleyuan.winter.aop.around;

import com.tripleyuan.winter.annotation.Autowired;
import com.tripleyuan.winter.annotation.Component;
import com.tripleyuan.winter.annotation.Order;

@Order(0)
@Component
public class OtherBean {

    public OriginBean origin;

    public OtherBean(@Autowired OriginBean origin) {
        this.origin = origin;
    }
}
