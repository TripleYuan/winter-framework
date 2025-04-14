package com.tripleyuan.winter.context;

import com.tripleyuan.winter.demo.DemoApplication;
import com.tripleyuan.winter.demo.bean.*;
import com.tripleyuan.winter.demo.config.BeanConfig;
import com.tripleyuan.winter.io.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class DemoApplicationTest {

    @Test
    public void testComponentAnnotation() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(DemoApplication.class, createPropertyResolver())) {
            assertThat(context.findBeanDefinition("dogBean")).isNotNull();
            assertThat(context.findBeanDefinition("AppleBean")).isNotNull();
            assertThat(context.findBeanDefinition("appleBean")).isNull();
        }
    }

    @Test
    public void testConfiguration() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(DemoApplication.class, createPropertyResolver())) {
            assertThat(context.findBeanDefinition("dogBean")).isNotNull();
            assertThat(context.findBeanDefinition("AppleBean")).isNotNull();
            assertThat(context.findBeanDefinition("appleBean")).isNull();
            assertThat(context.findBeanDefinition("catBean")).isNotNull();
        }
    }

    @Test
    public void findBeanDefinition_type() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(DemoApplication.class, createPropertyResolver())) {
            assertThat(context.findBeanDefinition(CatBean.class)).isNotNull();
            assertThat(context.findBeanDefinition(DogBean.class)).isNotNull();
            assertThat(context.findBeanDefinition(AppleBean.class)).isNotNull();
        }
    }

    @Test
    public void testGetBean() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(DemoApplication.class, createPropertyResolver())) {

            assertThat((Object) context.getBean("AppleBean")).isEqualTo(context.getBean(AppleBean.class));

            assertThat(context.getBeans(BeanConfig.class)).isNotNull();
            assertThat(context.getBean(CatBean.class)).isNotNull();
            assertThat(context.getBean(DogBean.class)).isNotNull();
            assertThat(context.getBean(MonkeyBean.class)).isNotNull();
            assertThat(context.getBean(AppleBean.class)).isNotNull();
            assertThat(context.getBean(BananaBean.class)).isNotNull();

            MonkeyBean monkeyBean = context.getBean("monkeyBean");
            BananaBean bananaBean = context.getBean("bananaBean");
            assertThat(monkeyBean.getBananaBean()).isEqualTo(bananaBean);

            assertThat(context.getBeans(Bird.class)).isNotNull();
            assertThat(context.getBeans(Magpie.class)).isNotNull();
            assertThat(context.getBeans(Eagle.class)).isNotNull();
            Bird bird = context.getBean(Bird.class);
            Magpie magpie = context.getBean(Magpie.class);
            assertThat(bird).isEqualTo(magpie);

            A a = context.getBean(A.class);
            assertThat(a).isNotNull();
            assertThat(a.getB()).isNotNull();
            assertThat(a.getB().getC()).isNotNull();
        }
    }

    @Test
    public void testCyclicDependency() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(DemoApplication.class, createPropertyResolver())) {
            CyclicBean1 b1 = context.getBean(CyclicBean1.class);
            CyclicBean2 b2 = context.getBean(CyclicBean2.class);
            assertThat(b1.getCyclicBean2()).isSameAs(b2);
            assertThat(b2.getCyclicBean1()).isSameAs(b1);
        }
    }

    PropertyResolver createPropertyResolver() {
        Properties ps = new Properties();
        return new PropertyResolver(ps);
    }
}
