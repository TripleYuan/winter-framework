package com.tripleyuan.winter.web;

import com.tripleyuan.winter.context.AnnotationConfigApplicationContext;
import com.tripleyuan.winter.context.ApplicationContext;
import com.tripleyuan.winter.exception.NestedRuntimeException;
import com.tripleyuan.winter.io.PropertyResolver;
import com.tripleyuan.winter.utils.WebUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("init {}", getClass().getName());

        ServletContext servletContext = sce.getServletContext();
        WebMvcConfiguration.setServletContext(servletContext);
        // create PropertyResolver
        PropertyResolver propertyResolver = WebUtils.createPropertyResolver();

        // set encoding
        String encoding = propertyResolver.getProperty("${winter.web.character-encoding:UTF-8}");
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);

        // create ApplicationContext
        String configClass = servletContext.getInitParameter("configuration");
        ApplicationContext applicationContext = createApplicationContext(configClass, propertyResolver);
        servletContext.setAttribute("applicationContext", applicationContext);

        // register filters
        WebUtils.registerFilters(servletContext, applicationContext);
        // register DispatcherServlet
        WebUtils.registerDispatcherServlet(servletContext, applicationContext, propertyResolver);
    }

    private ApplicationContext createApplicationContext(String configClass, PropertyResolver propertyResolver) {
        Class<?> clz = null;
        try {
            clz = Class.forName(configClass);
        } catch (ClassNotFoundException e) {
            throw new NestedRuntimeException("Could not load class from init param 'configuration': " + configClass, e);
        }
        return new AnnotationConfigApplicationContext(clz, propertyResolver);
    }
}
