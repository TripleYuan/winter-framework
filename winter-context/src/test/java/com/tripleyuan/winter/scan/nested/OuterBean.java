package com.tripleyuan.winter.scan.nested;

import com.tripleyuan.winter.annotation.Component;

@Component
public class OuterBean {

    @Component
    public static class NestedBean {

    }
}
