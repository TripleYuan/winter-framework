package com.tripleyuan.winter.scan.destroy;

import com.tripleyuan.winter.annotation.Value;
import com.tripleyuan.winter.annotation.Component;
import jakarta.annotation.PreDestroy;

@Component
public class AnnotationDestroyBean {

    @Value("${app.title}")
    public String appTitle;

    @PreDestroy
    void destroy() {
        this.appTitle = null;
    }

}
