package com.tripleyuan.winter.aop;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
public class ProxyResolver {

    final ByteBuddy byteBuddy = new ByteBuddy();

    private static final ProxyResolver INSTANCE = new ProxyResolver();

    public static ProxyResolver getInstance() {
        return INSTANCE;
    }

    private ProxyResolver() {
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(T bean, InvocationHandler handler) {
        Class<?> targetClass = bean.getClass();
        log.debug("create proxy for bean {} @{}", targetClass.getName(), Integer.toHexString(bean.hashCode()));

        Class<?> proxyClass = this.byteBuddy
                // subclass with default empty constructor:
                .subclass(targetClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                // intercept methods:
                .method(ElementMatchers.isPublic()).intercept(InvocationHandlerAdapter.of(
                        // proxy method invoke:
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                // delegate to origin bean:
                                return handler.invoke(bean, method, args);
                            }
                        }))
                // generate proxy class:
                .make().load(targetClass.getClassLoader()).getLoaded();

        Object proxy;
        try {
            proxy = proxyClass.getConstructor().newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) proxy;
    }

}

