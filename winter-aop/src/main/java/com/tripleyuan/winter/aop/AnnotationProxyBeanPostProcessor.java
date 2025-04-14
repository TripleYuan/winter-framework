package com.tripleyuan.winter.aop;

import com.tripleyuan.winter.context.BeanDefinition;
import com.tripleyuan.winter.context.BeanPostProcessor;
import com.tripleyuan.winter.context.ConfigurableApplicationContext;
import com.tripleyuan.winter.exception.AopConfigException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.tripleyuan.winter.context.ApplicationContextUtils.getRequiredApplicationContext;

public abstract class AnnotationProxyBeanPostProcessor<A extends Annotation> implements BeanPostProcessor {

    private final Map<String, Object> originBeans = new HashMap<>(128);
    private Class<A> annotationClass;

    public AnnotationProxyBeanPostProcessor() {
        this.annotationClass = getParameterizedType();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> targetClass = bean.getClass();
        A anno = targetClass.getAnnotation(annotationClass);
        if (anno == null) {
            return bean;
        }

        String handlerName = null;
        try {
            handlerName = (String) annotationClass.getMethod("value").invoke(anno);
        } catch (Exception e) {
            throw new AopConfigException(String.format("@%s must have value() returned String type.", annotationClass.getSimpleName()));
        }

        Object proxy = createProxy(bean, handlerName);
        originBeans.put(beanName, bean);
        return proxy;
    }

    private Object createProxy(Object bean, String handlerName) {
        ConfigurableApplicationContext cac = (ConfigurableApplicationContext) getRequiredApplicationContext();
        BeanDefinition def = cac.findBeanDefinition(handlerName);
        if (def == null) {
            throw new AopConfigException(String.format("No bean found with name '%s'", handlerName));
        }
        Object handlerBean = def.getInstance();
        if (handlerBean == null) {
            handlerBean = cac.createBeanAsEarlySingleton(def);
        }
        if (handlerBean instanceof InvocationHandler) {
            return ProxyResolver.getInstance().createProxy(bean, (InvocationHandler) handlerBean);
        }
        throw new AopConfigException(String.format("Handler bean '%s' must implement InvocationHandler", handlerName));
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = originBeans.get(beanName);
        return origin == null ? bean : origin;
    }


    @SuppressWarnings("unchecked")
    private Class<A> getParameterizedType() {
        Type type = getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type.");
        }
        ParameterizedType pt = (ParameterizedType) type;
        Type[] types = pt.getActualTypeArguments();
        if (types.length != 1) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " has more than 1 parameterized types.");
        }
        Type r = types[0];
        if (!(r instanceof Class<?>)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type of class.");
        }
        return (Class<A>) r;
    }

}
