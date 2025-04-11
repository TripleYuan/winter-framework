package com.tripleyuan.winter.demo.config;

import com.tripleyuan.winter.annotation.Bean;
import com.tripleyuan.winter.annotation.Configuration;
import com.tripleyuan.winter.demo.bean.CatBean;

@Configuration
public class AnimalConfig {

    @Bean
    public CatBean catBean(){
        return new CatBean();
    }

}
