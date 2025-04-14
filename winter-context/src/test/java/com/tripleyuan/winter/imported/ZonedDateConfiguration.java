package com.tripleyuan.winter.imported;

import com.tripleyuan.winter.annotation.Bean;
import com.tripleyuan.winter.annotation.Configuration;

import java.time.ZonedDateTime;

@Configuration
public class ZonedDateConfiguration {

    @Bean
    ZonedDateTime startZonedDateTime() {
        return ZonedDateTime.now();
    }
}
