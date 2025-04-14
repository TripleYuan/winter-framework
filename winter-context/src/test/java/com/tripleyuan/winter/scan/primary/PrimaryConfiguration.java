package com.tripleyuan.winter.scan.primary;

import com.tripleyuan.winter.annotation.Bean;
import com.tripleyuan.winter.annotation.Configuration;
import com.tripleyuan.winter.annotation.Primary;

@Configuration
public class PrimaryConfiguration {

    @Primary
    @Bean
    DogBean husky() {
        return new DogBean("Husky");
    }

    @Bean
    DogBean teddy() {
        return new DogBean("Teddy");
    }
}
