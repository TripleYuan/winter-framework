package com.tripleyuan.winter.context;

import jakarta.annotation.Nullable;

import java.util.List;

public interface ConfigurableApplicationContext extends ApplicationContext {

    @Nullable
    BeanDefinition findBeanDefinition(String beanName);

    @Nullable
    BeanDefinition findBeanDefinition(String beanName, Class<?> requiredType);

    @Nullable
    BeanDefinition findBeanDefinition(Class<?> requiredType);

    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    Object createBeanAsEarlySingleton(BeanDefinition def);

}
