package com.tripleyuan.winter.scan.destroy;

public class SpecifyDestroyBean {

    public String appTitle;

    SpecifyDestroyBean(String appTitle) {
        this.appTitle = appTitle;
    }

    void destroy() {
        this.appTitle = null;
    }
}
