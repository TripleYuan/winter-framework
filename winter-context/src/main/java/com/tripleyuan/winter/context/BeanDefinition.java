package com.tripleyuan.winter.context;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@Getter
@Setter
@ToString
public class BeanDefinition {

    // 全局唯一的Bean Name
    private String name;

    // Bean的声明类型
    private Class<?> beanClass;

    // Bean的实例
    private Object instance = null;

    // 构造方法/null
    private Constructor<?> constructor;

    // 工厂名称/null
    private String factoryName;

    // 工厂方法/null
    private Method factoryMethod;

    // Bean的顺序
    private int order;

    // 是否标识@Primary
    private boolean primary;

    // init/destroy方法名称
    private String initMethodName;
    private String destroyMethodName;

    // init/destroy方法
    private Method initMethod;
    private Method destroyMethod;
}
