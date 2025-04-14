package com.tripleyuan.winter.scan.proxy;

import com.tripleyuan.winter.annotation.Autowired;
import com.tripleyuan.winter.annotation.Component;

@Component
public class InjectProxyOnConstructorBean {

    public final OriginBean injected;

    public InjectProxyOnConstructorBean(@Autowired OriginBean injected) {
        this.injected = injected;
    }
}
