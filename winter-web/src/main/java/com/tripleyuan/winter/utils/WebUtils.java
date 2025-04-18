package com.tripleyuan.winter.utils;

import com.tripleyuan.winter.context.ApplicationContext;
import com.tripleyuan.winter.io.PropertyResolver;
import com.tripleyuan.winter.web.DispatcherServlet;
import com.tripleyuan.winter.web.FilterRegistrationBean;
import jakarta.annotation.Nullable;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class WebUtils {

    public static final String DEFAULT_PARAM_VALUE = "\0\t\0\t\0";

    public static final String CONFIG_APP_YML = "/application.yml";
    public static final String CONFIG_APP_YAML = "/application.yaml";
    public static final String CONFIG_APP_PROP = "/application.properties";

    public static PropertyResolver createPropertyResolver() {
        // try yml first, then yaml, last properties
        try {
            PropertyResolver pr = createPropertyResolverFromYml(CONFIG_APP_YAML);
            if (pr != null) {
                return pr;
            }

            pr = createPropertyResolverFromYml(CONFIG_APP_YML);
            if (pr != null) {
                return pr;
            }

            return createPropertyResolverFromProperties(CONFIG_APP_PROP);
        } catch (Exception e) {
            log.warn("load config failed.", e);
        }
        return new PropertyResolver(new Properties());
    }

    @Nullable
    private static PropertyResolver createPropertyResolverFromYml(String path) {
        try {
            Properties props = new Properties();
            Map<String, Object> map = YamlUtils.loadYaml(CONFIG_APP_YAML);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    props.put(entry.getKey(), value);
                } else {
                    log.warn("Found unsupported config param, type: {}, value: {}", value.getClass(), value);
                }
            }
            log.info("load config with yaml format: {}", path);
            return new PropertyResolver(props);
        } catch (Exception e) {
            if (e.getCause() instanceof FileNotFoundException) {
                return null;
            }
            throw e;
        }
    }

    private static PropertyResolver createPropertyResolverFromProperties(String path) {
        try {
            Properties props = new Properties();
            ClassPathUtils.readInputStream(path, in -> {
                props.load(in);
                return true;
            });
            log.info("load config with properties: {}", path);
            return new PropertyResolver(props);
        } catch (Exception e) {
            if (e.getCause() instanceof FileNotFoundException) {
                return null;
            }
            throw e;
        }
    }


    public static void registerDispatcherServlet(ServletContext servletContext,
                                                 ApplicationContext applicationContext,
                                                 PropertyResolver propertyResolver) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext, propertyResolver);
        log.info("register servlet {} for URL '/'", dispatcherServlet.getClass().getName());
        ServletRegistration.Dynamic reg = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        reg.addMapping("/");
        reg.setLoadOnStartup(0);
    }

    public static void registerFilters(ServletContext servletContext, ApplicationContext applicationContext) {
        List<FilterRegistrationBean> filterRegBeans = applicationContext.getBeans(FilterRegistrationBean.class);
        for (FilterRegistrationBean filterRegBean : filterRegBeans) {
            FilterRegistration.Dynamic reg = servletContext.addFilter(filterRegBean.getName(), filterRegBean.getFilter());
            String[] urlPatterns = filterRegBean.getUrlPatterns().toArray(new String[0]);
            reg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, urlPatterns);
        }
    }
}
