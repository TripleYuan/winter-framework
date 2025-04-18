package com.tripleyuan.winter.controller;

import com.tripleyuan.winter.annotation.Configuration;
import com.tripleyuan.winter.annotation.Import;
import com.tripleyuan.winter.web.WebMvcConfiguration;

@Configuration
@Import(WebMvcConfiguration.class)
public class ControllerConfiguration {

}
