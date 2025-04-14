package com.tripleyuan.winter.aop.before;

import com.tripleyuan.winter.annotation.Bean;
import com.tripleyuan.winter.annotation.ComponentScan;
import com.tripleyuan.winter.annotation.Configuration;
import com.tripleyuan.winter.aop.AroundProxyBeanPostProcessor;

@Configuration
@ComponentScan
public class BeforeApplication {

    @Bean
    public AroundProxyBeanPostProcessor aroundProxyBeanPostProcessor() {
        return new AroundProxyBeanPostProcessor();
    }

}
