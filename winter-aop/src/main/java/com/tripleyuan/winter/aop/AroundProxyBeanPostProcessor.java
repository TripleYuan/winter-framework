package com.tripleyuan.winter.aop;

import com.tripleyuan.winter.annotation.Around;
import com.tripleyuan.winter.context.BeanDefinition;
import com.tripleyuan.winter.context.BeanPostProcessor;
import com.tripleyuan.winter.context.ConfigurableApplicationContext;
import com.tripleyuan.winter.exception.AopConfigException;

import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.Map;

import static com.tripleyuan.winter.context.ApplicationContextUtils.getRequiredApplicationContext;

public class AroundProxyBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Around> {

}
