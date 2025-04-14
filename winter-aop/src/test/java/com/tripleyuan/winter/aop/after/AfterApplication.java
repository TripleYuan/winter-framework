package com.tripleyuan.winter.aop.after;

import com.tripleyuan.winter.annotation.Bean;
import com.tripleyuan.winter.annotation.ComponentScan;
import com.tripleyuan.winter.annotation.Configuration;
import com.tripleyuan.winter.aop.AroundProxyBeanPostProcessor;

@Configuration
@ComponentScan
public class AfterApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor() {
        return new AroundProxyBeanPostProcessor();
    }
}
