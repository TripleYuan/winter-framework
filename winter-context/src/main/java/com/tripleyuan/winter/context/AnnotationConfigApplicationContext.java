package com.tripleyuan.winter.context;

import com.tripleyuan.winter.annotation.*;
import com.tripleyuan.winter.exception.BeanCreationException;
import com.tripleyuan.winter.exception.BeanDefinitionException;
import com.tripleyuan.winter.io.PropertyResolver;
import com.tripleyuan.winter.io.ResourceResolver;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.tripleyuan.winter.io.ResourceResolver.FULL_CLASS_NAME_MAPPER;
import static com.tripleyuan.winter.utils.ClassUtils.*;
import static java.util.stream.Collectors.toList;

@Slf4j
public class AnnotationConfigApplicationContext implements ApplicationContext {

    private Map<String, BeanDefinition> beans;

    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) {
        Set<String> candidateClassNames = scanForClassNames(configClass);
        this.beans = createBeanDefinitions(candidateClassNames);
    }

    private Set<String> scanForClassNames(Class<?> configClass) {
        ComponentScan componentScan = findAnnotation(configClass, ComponentScan.class);
        String[] basePackages = componentScan == null ? new String[]{configClass.getPackage().getName()} : componentScan.value();

        Set<String> classNames = new HashSet<>();
        // scan package
        for (String basePackage : basePackages) {
            log.debug("Scan package: {}", basePackage);
            ResourceResolver rr = new ResourceResolver(basePackage);
            List<String> scanedList = rr.scan(FULL_CLASS_NAME_MAPPER);

            if (log.isDebugEnabled()) {
                scanedList.forEach(className -> log.debug("class found by component scan: {}", className));
            }
            classNames.addAll(scanedList);
        }

        // handle @Import
        Import importConfig = findAnnotation(configClass, Import.class);
        if (importConfig != null) {
            for (Class<?> clazz : importConfig.value()) {
                String importClassName = clazz.getName();
                if (classNames.contains(importClassName)) {
                    log.warn("ignore import {} for it's already been scanned.", importClassName);
                } else {
                    log.debug("class found by import: {}", importClassName);
                    classNames.add(importClassName);
                }
            }
        }

        return classNames;
    }

    private Map<String, BeanDefinition> createBeanDefinitions(Set<String> candidateClassNames) {
        Map<String, BeanDefinition> defs = new HashMap<>();
        for (String className : candidateClassNames) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new BeanCreationException(e);
            }

            if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation()) {
                continue;
            }

            Component component = findAnnotation(clazz, Component.class);
            if (component != null) {
                int mod = clazz.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    throw new BeanCreationException("@Component class " + className + " must not be abstract.");
                }
                if (Modifier.isPrivate(mod)) {
                    throw new BeanCreationException("@Component class " + className + " must not be private.");
                }

                BeanDefinition def = new BeanDefinition();
                def.setName(getBeanName(clazz));
                def.setBeanClass(clazz);
                def.setConstructor(getSutableConstructor(clazz));
                def.setOrder(getOrder(clazz));
                def.setPrimary(findAnnotation(clazz, Primary.class) != null);
                def.setInitMethod(findAnnotationMethod(clazz, PostConstruct.class));
                def.setDestroyMethod(findAnnotationMethod(clazz, PreDestroy.class));
                addBeanDefinition(defs, def);

                // configuration class?
                Configuration configuration = findAnnotation(clazz, Configuration.class);
                if (configuration != null) {
                    if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                        throw new BeanDefinitionException("@Configuration class " + className + " cannot be BeanPostProcessor.");
                    }
                    scanFactoryMethods(className, clazz, defs);
                }
            }
        }
        return defs;
    }

    // get public constructor or non-public constructor as fallback.
    private Constructor<?> getSutableConstructor(Class<?> clazz) {
        // public
        Constructor<?>[] cons = clazz.getDeclaredConstructors();
        if (cons.length > 0) {
            if (cons.length == 1) {
                return cons[0];
            }
            throw new BeanCreationException("More than one public constructor found in class " + clazz.getName());
        }

        // non-public
        cons = clazz.getConstructors();
        if (cons.length == 0) {
            throw new BeanCreationException("No constructor found in class " + clazz.getName());
        }
        if (cons.length > 1) {
            throw new BeanCreationException("More than one constructor found in class " + clazz.getName());
        }
        return cons[0];
    }

    private int getOrder(Class<?> clazz) {
        Order order = findAnnotation(clazz, Order.class);
        return order == null ? Order.LOWEST_ORDER : order.value();
    }

    private int getOrder(Method method) {
        Order order = method.getAnnotation(Order.class);
        return order == null ? Order.LOWEST_ORDER : order.value();
    }

    // scan factory method in @Configuration class
    private void scanFactoryMethods(String factoryBeanName, Class<?> configClazz, Map<String, BeanDefinition> defs) {
        Method[] candidateBeanMethods = configClazz.getDeclaredMethods();
        for (Method method : candidateBeanMethods) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean == null) {
                continue;
            }

            // check modifier
            int mod = method.getModifiers();
            if (Modifier.isAbstract(mod)) {
                throw new BeanDefinitionException("@Bean method " + configClazz.getName() + "." + method.getName() + " must not be abstract.");
            }
            if (Modifier.isFinal(mod)) {
                throw new BeanDefinitionException("@Bean method " + configClazz.getName() + "." + method.getName() + " must not be final.");
            }
            if (Modifier.isPrivate(mod)) {
                throw new BeanDefinitionException("@Bean method " + configClazz.getName() + "." + method.getName() + " must not be private.");
            }

            Class<?> beanClass = method.getReturnType();
            if (beanClass.isPrimitive()) {
                throw new BeanDefinitionException("@Bean method " + configClazz.getName() + "." + method.getName() + " must not return primitive type.");
            }
            if (beanClass == void.class || beanClass == Void.class) {
                throw new BeanDefinitionException("@Bean method " + configClazz.getName() + "." + method.getName() + " must not return void.");
            }
            log.info("@Bean method {}.{} found in config class.", configClazz.getName(), method.getName());

            BeanDefinition def = new BeanDefinition();
            def.setName(getBeanName(method));
            def.setBeanClass(beanClass);
            def.setFactoryName(factoryBeanName);
            def.setFactoryMethod(method);
            def.setInitMethodName(bean.initMethod().isEmpty() ? null : bean.initMethod());
            def.setDestroyMethodName(bean.destroyMethod().isEmpty() ? null : bean.destroyMethod());
            def.setOrder(getOrder(method));
            def.setPrimary(method.isAnnotationPresent(Primary.class));
            addBeanDefinition(defs, def);
        }
    }

    // add bean definition to map. If bean name already exists, throw exception.
    private void addBeanDefinition(Map<String, BeanDefinition> defs, BeanDefinition def) {
        if (defs.put(def.getName(), def) != null) {
            throw new BeanDefinitionException("Duplicate bean name: " + def.getName());
        }
    }

    @Nullable
    public BeanDefinition findBeanDefinition(String beanName) {
        return this.beans.get(beanName);
    }

    @Nullable
    public BeanDefinition findBeanDefinition(Class<?> type) {
        List<BeanDefinition> beanDefinitions = findBeanDefinitions(type);
        if (beanDefinitions.isEmpty()) {
            return null;
        }
        if (beanDefinitions.size() == 1) {
            return beanDefinitions.get(0);
        }

        // Found multiple bean, filter bean with @Primary.
        List<BeanDefinition> primaryList = beanDefinitions.stream().filter(t -> t.isPrimary()).collect(toList());
        if (primaryList.size() == 1) {
            return primaryList.get(0);
        }

        if (primaryList.isEmpty()) {
            throw new BeanDefinitionException(String.format("Multiple bean with type '%s' found, but no @Primary specified.", type.getName()));
        } else {
            throw new BeanDefinitionException(String.format("Multiple bean with type '%s' found, and multiple @Primary specified.", type.getName()));
        }

    }

    private List<BeanDefinition> findBeanDefinitions(Class<?> type) {
        return beans.values().stream()
                .filter(bd -> type.isAssignableFrom(bd.getBeanClass()))
                .collect(toList());
    }

    @Override
    public boolean existsBean(String beanName) {
        return false;
    }

    @Override
    public <T> T getBean(String beanName) {
        return null;
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return null;
    }

    @Override
    public <T> List<T> getBeans(Class<T> requiredType) {
        return Collections.emptyList();
    }

    @Override
    public void close() {

    }
}
