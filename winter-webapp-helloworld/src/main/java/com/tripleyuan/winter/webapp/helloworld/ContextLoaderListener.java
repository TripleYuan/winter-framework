package com.tripleyuan.winter.webapp.helloworld;

import com.tripleyuan.winter.context.AnnotationConfigApplicationContext;
import com.tripleyuan.winter.context.ApplicationContext;
import com.tripleyuan.winter.io.PropertyResolver;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextLoaderListener implements ServletContextListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("init {}.", getClass().getName());
        ServletContext servletContext = sce.getServletContext();
        PropertyResolver propertyResolver = WebUtils.createPropertyResolver();
        String encoding = propertyResolver.getProperty("${winter.web.character-encoding:UTF-8}");
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);
        ApplicationContext applicationContext =
                createApplicationContext(servletContext.getInitParameter("configuration"), propertyResolver);
        // register DispatcherServlet:
        WebUtils.registerDispatcherServlet(servletContext, propertyResolver);

        servletContext.setAttribute("applicationContext", applicationContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Object applicationContext = sce.getServletContext().getAttribute("applicationContext");
        if (applicationContext instanceof ApplicationContext) {
            ((ApplicationContext) applicationContext).close();
        }
    }

    ApplicationContext createApplicationContext(String configClassName, PropertyResolver propertyResolver) {
        logger.info("init ApplicationContext by configuration: {}", configClassName);
        if (configClassName == null || configClassName.isEmpty()) {
            throw new RuntimeException("Cannot init ApplicationContext for missing init param name: configuration");
        }
        Class<?> configClass;
        try {
            configClass = Class.forName(configClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load class from init param 'configuration': " + configClassName);
        }
        return new AnnotationConfigApplicationContext(configClass, propertyResolver);
    }
}
