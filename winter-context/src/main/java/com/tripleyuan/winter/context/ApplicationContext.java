package com.tripleyuan.winter.context;

import com.tripleyuan.winter.exception.NoSuchBeanDefinitionException;

import java.util.List;

public interface ApplicationContext extends AutoCloseable {

    boolean existsBean(String beanName);

    /**
     * find bean by name, if not exists, throw {@link NoSuchBeanDefinitionException}
     */
    <T> T getBean(String beanName);

    /**
     * find bean by name and type, if not exists, throw {@link NoSuchBeanDefinitionException}
     */
    <T> T getBean(String beanName, Class<T> requiredType);

    /**
     * find bean by type, if not exists, throw {@link NoSuchBeanDefinitionException}
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * find beans by type, if not exists, return empty list.
     */
    <T> List<T> getBeans(Class<T> requiredType);

    /**
     * will invoke all bean's destroy method.
     */
    void close();
}
