package com.tripleyuan.winter.scan.destroy;

import com.tripleyuan.winter.annotation.Bean;
import com.tripleyuan.winter.annotation.Configuration;
import com.tripleyuan.winter.annotation.Value;

@Configuration
public class SpecifyDestroyConfiguration {

    @Bean(destroyMethod = "destroy")
    SpecifyDestroyBean createSpecifyDestroyBean(@Value("${app.title}") String appTitle) {
        return new SpecifyDestroyBean(appTitle);
    }
}
