package com.tripleyuan.winter.web;

import com.tripleyuan.winter.annotation.Autowired;
import com.tripleyuan.winter.annotation.Bean;
import com.tripleyuan.winter.annotation.Configuration;
import com.tripleyuan.winter.annotation.Value;
import jakarta.servlet.ServletContext;

import java.util.Objects;

@Configuration
public class WebMvcConfiguration {

    private static ServletContext servletContext = null;

    /**
     * Set by web listener.
     */
    static void setServletContext(ServletContext ctx) {
        servletContext = ctx;
    }

    // @Bean(initMethod = "init")
    // ViewResolver viewResolver( //
    //         @Autowired ServletContext servletContext, //
    //         @Value("${summer.web.freemarker.template-path:/WEB-INF/templates}") String templatePath, //
    //         @Value("${summer.web.freemarker.template-encoding:UTF-8}") String templateEncoding) {
    //     return new FreeMarkerViewResolver(servletContext, templatePath, templateEncoding);
    // }

    @Bean
    ServletContext servletContext() {
        return Objects.requireNonNull(servletContext, "ServletContext is not set.");
    }
}
