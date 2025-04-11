package com.tripleyuan.winter.context;

import com.tripleyuan.winter.exception.BeanCreationException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

@ToString
public class BeanDefinition implements Comparable<BeanDefinition> {

    // 全局唯一的Bean Name
    @Getter
    @Setter
    private String name;

    // Bean的声明类型
    @Getter
    @Setter
    private Class<?> beanClass;

    // Bean的实例
    @Getter
    private Object instance = null;

    // 构造方法/null
    @Getter
    private Constructor<?> constructor;

    // 工厂名称/null
    @Getter
    @Setter
    private String factoryName;

    // 工厂方法/null
    @Getter
    private Method factoryMethod;

    // Bean的顺序
    @Getter
    @Setter
    private int order;

    // 是否标识@Primary
    @Getter
    @Setter
    private boolean primary;

    @Getter
    @Setter
    private String initMethodName;

    @Getter
    @Setter
    private String destroyMethodName;

    @Getter
    private Method initMethod;

    @Getter
    private Method destroyMethod;

    public Object getRequiredInstance() {
        if (this.instance == null) {
            throw new BeanCreationException(String.format("Instance of bean with name '%s' and type '%s' is not instantiated during current stage.",
                    this.getName(), this.getBeanClass().getName()));
        }
        return this.instance;
    }

    public void setInstance(Object instance) {
        Objects.requireNonNull(instance, "Bean instance is null.");
        if (!this.beanClass.isAssignableFrom(instance.getClass())) {
            throw new BeanCreationException(String.format("Instance '%s' of Bean '%s' is not the expected type: %s",
                    instance, instance.getClass().getName(), this.beanClass.getName()));
        }
        this.instance = instance;
    }

    public void setConstructor(Constructor<?> constructor) {
        Objects.requireNonNull(constructor, "Constructor is null.");
        this.constructor = constructor;
        this.constructor.setAccessible(true);
    }

    public void setFactoryMethod(Method factoryMethod) {
        Objects.requireNonNull(factoryMethod, "factoryMethod is null.");
        this.factoryMethod = factoryMethod;
        this.factoryMethod.setAccessible(true);
    }

    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
        if (initMethod != null) {
            this.initMethod.setAccessible(true);
        }
    }

    public void setDestroyMethod(Method destroyMethod) {
        this.destroyMethod = destroyMethod;
        if (destroyMethod != null) {
            this.destroyMethod.setAccessible(true);
        }
    }

    @Override
    public int compareTo(BeanDefinition o) {
        int val = Integer.compare(this.order, o.order);
        if (val == 0) {
            val = this.name.compareTo(o.name);
        }
        return val;
    }
}
