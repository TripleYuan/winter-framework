package com.tripleyuan.winter.context;

import com.tripleyuan.winter.annotation.*;
import com.tripleyuan.winter.exception.*;
import com.tripleyuan.winter.io.PropertyResolver;
import com.tripleyuan.winter.io.ResourceResolver;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static com.tripleyuan.winter.io.ResourceResolver.FULL_CLASS_NAME_MAPPER;
import static com.tripleyuan.winter.utils.ClassUtils.*;
import static java.util.stream.Collectors.toList;

@Slf4j
public class AnnotationConfigApplicationContext implements ApplicationContext {

    // map: beanName -> beanDefinition
    private Map<String, BeanDefinition> beans;
    // Beans which are in creation
    private Set<String> creatingBeanNames = new HashSet<>(256);
    // propertyResolver
    private PropertyResolver propertyResolver;

    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;

        // scan
        Set<String> candidateClassNames = scanForClassNames(configClass);

        // create bean definitions
        this.beans = createBeanDefinitions(candidateClassNames);

        // create Configuration beans
        this.beans.values().stream()
                .filter(this::isConfigurationBean)
                .forEach(this::createBeanAsEarlySingleton);

        // create BeanPostProcessor beans
        this.beans.values().stream()
                .filter(this::isBeanPostProcessorDefinition)
                .forEach(this::createBeanAsEarlySingleton);

        // create normal beans
        List<BeanDefinition> normalBeans = this.beans.values().stream()
                .filter(def -> def.getInstance() == null)
                .collect(toList());
        normalBeans.forEach(def -> {
            // need check null, because it may be created during other bean's creation.
            if (def.getInstance() == null) {
                createBeanAsEarlySingleton(def);
            }
        });
    }

    @Nullable
    public BeanDefinition findBeanDefinition(String beanName) {
        return this.beans.get(beanName);
    }

    @Nullable
    public BeanDefinition findBeanDefinition(String beanName, Class<?> requiredType) {
        BeanDefinition def = this.beans.get(beanName);
        if (def == null) {
            return null;
        }
        if (!requiredType.isAssignableFrom(def.getBeanClass())) {
            throw new BeanNotOfRequiredTypeException(String.format("Autowire required type '%s' but bean '%s' has actual type '%s'.",
                    requiredType.getName(), beanName, def.getBeanClass().getName()));
        }
        return def;
    }

    @Nullable
    public BeanDefinition findBeanDefinition(Class<?> requiredType) {
        List<BeanDefinition> defs = findBeanDefinitions(requiredType);
        if (defs.isEmpty()) {
            return null;
        }
        if (defs.size() == 1) {
            return defs.get(0);
        }

        // Found multiple bean, filter bean with @Primary.
        List<BeanDefinition> primaryList = defs.stream().filter(t -> t.isPrimary()).collect(toList());
        if (primaryList.size() == 1) {
            return primaryList.get(0);
        }

        if (primaryList.isEmpty()) {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, but no @Primary specified.", requiredType.getName()));
        } else {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, and multiple @Primary specified.", requiredType.getName()));
        }

    }

    private List<BeanDefinition> findBeanDefinitions(Class<?> type) {
        return beans.values().stream()
                .filter(bd -> type.isAssignableFrom(bd.getBeanClass()))
                .collect(toList());
    }

    @Override
    public boolean existsBean(String beanName) {
        return beans.containsKey(beanName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName) {
        BeanDefinition def = findBeanDefinition(beanName);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(String.format("No bean found with name '%s'", beanName));
        }
        return (T) def.getRequiredInstance();
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) {
        T bean = findBean(beanName, requiredType);
        if (bean == null) {
            throw new NoSuchBeanDefinitionException(String.format("No bean found with name '%s' and type '%s'", beanName, requiredType.getName()));
        }
        return bean;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        T bean = findBean(requiredType);
        if (bean == null) {
            throw new NoSuchBeanDefinitionException(String.format("No bean found with type '%s'", requiredType.getName()));
        }
        return bean;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeans(Class<T> requiredType) {
        return findBeanDefinitions(requiredType).stream()
                .map(def -> (T) def.getRequiredInstance())
                .collect(toList());
    }

    // findBean return null if bean no exists.
    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> T findBean(String beanName, Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(beanName, requiredType);
        if (def == null) {
            return null;
        }
        return (T) def.getRequiredInstance();
    }

    // findBean return null if bean no exists.
    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> T findBean(Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(requiredType);
        if (def == null) {
            return null;
        }
        return (T) def.getRequiredInstance();
    }

    @Override
    public void close() {

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
        Map<String, BeanDefinition> defs = new HashMap<>(256);
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

                String beanName = getBeanName(clazz);

                BeanDefinition def = new BeanDefinition();
                def.setName(beanName);
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
                    scanFactoryMethods(beanName, clazz, defs);
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
            log.debug("@Bean method {}.{} found in config class.", configClazz.getName(), method.getName());

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

    private boolean isConfigurationBean(BeanDefinition def) {
        return findAnnotation(def.getBeanClass(), Configuration.class) != null;
    }

    private boolean isBeanPostProcessorDefinition(BeanDefinition def) {
        return BeanPostProcessor.class.isAssignableFrom(def.getBeanClass());
    }

    private Object createBeanAsEarlySingleton(BeanDefinition def) {
        log.debug("Try to create bean '{}' as early singleton.", def.getName());
        if (!creatingBeanNames.add(def.getName())) {
            throw new BeanCreationException("Found cyclic dependency when creating bean '" + def.getName() + "'");
        }

        // create bean instance by constructor or factory method
        Executable createFn = def.getFactoryName() == null ? def.getConstructor() : def.getFactoryMethod();

        // resolve parameter
        final Parameter[] parameters = createFn.getParameters();
        Annotation[][] parametersAnnos = createFn.getParameterAnnotations();
        final Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Parameter param = parameters[i];
            final Annotation[] paramAnnos = parametersAnnos[i];
            final Value value = getAnnotation(paramAnnos, Value.class);
            final Autowired autowired = getAnnotation(paramAnnos, Autowired.class);

            // Cannot create Configuration bean with @Autowired
            boolean isConfiguration = isConfigurationBean(def);
            if (isConfiguration && autowired != null) {
                throw new BeanCreationException(String.format("Cannot specify @Autowire when creating @Configuration bean '%s': %s",
                        def.getName(), def.getBeanClass().getName()));
            }

            // Cannot create BeanPostProcessor bean with @Autowired
            boolean isBeanPostProcessor = isBeanPostProcessorDefinition(def);
            if (isBeanPostProcessor && autowired != null) {
                throw new BeanCreationException(String.format("Cannot specify @Autowire when creating @BeanPostProcessor bean '%s': %s",
                        def.getName(), def.getBeanClass().getName()));
            }

            if (value == null && autowired == null) {
                throw new BeanCreationException(String.format("Must specify @Value or @Autowired when create bean '%s': %s",
                        def.getName(), def.getBeanClass().getName()));
            }
            if (value != null && autowired != null) {
                throw new BeanCreationException(String.format("Cannot specify both @Value and @Autowired when create bean '%s': %s",
                        def.getName(), def.getBeanClass().getName()));
            }

            Class<?> type = param.getType();
            if (value != null) {
                // 'value' parameter
                args[i] = propertyResolver.getRequiredProperty(value.value());
            } else {
                // 'autowired' parameter
                String name = autowired.name();
                boolean isRequired = autowired.value();
                BeanDefinition dependsOnDef = name.isEmpty() ? findBeanDefinition(type) : findBeanDefinition(name, type);
                if (isRequired && dependsOnDef == null) {
                    throw new BeanCreationException(String.format("Missing autowired bean with type '%s',  when create bean '%s': %s",
                            type.getName(), def.getName(), def.getBeanClass().getName()));
                }

                if (dependsOnDef != null) {
                    Object autowiredBeanInstance = dependsOnDef.getInstance();
                    if (autowiredBeanInstance == null) {
                        autowiredBeanInstance = createBeanAsEarlySingleton(dependsOnDef);
                    }
                    args[i] = autowiredBeanInstance;
                } else {
                    args[i] = null;
                }
            }
        }

        // instantiate bean
        Object beanInstance = null;
        if (createFn instanceof Constructor) {
            try {
                beanInstance = def.getConstructor().newInstance(args);
            } catch (Exception e) {
                throw new BeanDefinitionException(String.format("Exception when create bean '%s': %s", def.getName(), def.getBeanClass().getName()), e);
            }
        } else {
            Object configInstance = getBean(def.getFactoryName());
            try {
                beanInstance = def.getFactoryMethod().invoke(configInstance, args);
            } catch (Exception e) {
                throw new BeanDefinitionException(String.format("Exception when create bean '%s': %s", def.getName(), def.getBeanClass().getName()), e);
            }
        }
        def.setInstance(beanInstance);

        return beanInstance;
    }
}
