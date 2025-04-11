package com.tripleyuan.winter.context;

import com.tripleyuan.winter.demo.DemoApplication;
import com.tripleyuan.winter.demo.bean.*;
import com.tripleyuan.winter.demo.config.AnimalConfig;
import com.tripleyuan.winter.io.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationConfigApplicationContextTest {

    @Test
    public void testComponentAnnotation() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DemoApplication.class, createPropertyResolver());
        assertThat(context.findBeanDefinition("dogBean")).isNotNull();
        assertThat(context.findBeanDefinition("AppleBean")).isNotNull();
        assertThat(context.findBeanDefinition("appleBean")).isNull();
    }

    @Test
    public void testConfiguration() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DemoApplication.class, createPropertyResolver());
        assertThat(context.findBeanDefinition("dogBean")).isNotNull();
        assertThat(context.findBeanDefinition("AppleBean")).isNotNull();
        assertThat(context.findBeanDefinition("appleBean")).isNull();
        assertThat(context.findBeanDefinition("catBean")).isNotNull();
    }

    @Test
    public void findBeanDefinition_type() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DemoApplication.class, createPropertyResolver());
        assertThat(context.findBeanDefinition(CatBean.class)).isNotNull();
        assertThat(context.findBeanDefinition(DogBean.class)).isNotNull();
        assertThat(context.findBeanDefinition(AppleBean.class)).isNotNull();
    }

    @Test
    public void testGetBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DemoApplication.class, createPropertyResolver());

        assertThat((Object) context.getBean("AppleBean")).isEqualTo(context.getBean(AppleBean.class));

        assertThat(context.getBeans(AnimalConfig.class)).isNotNull();
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
    }

    PropertyResolver createPropertyResolver() {
        Properties ps = new Properties();
        ps.put("app.title", "Scan App");
        ps.put("app.version", "v1.0");
        ps.put("jdbc.url", "jdbc:hsqldb:file:testdb.tmp");
        ps.put("jdbc.username", "sa");
        ps.put("jdbc.password", "");
        ps.put("convert.boolean", "true");
        ps.put("convert.byte", "123");
        ps.put("convert.short", "12345");
        ps.put("convert.integer", "1234567");
        ps.put("convert.long", "123456789000");
        ps.put("convert.float", "12345.6789");
        ps.put("convert.double", "123456789.87654321");
        ps.put("convert.localdate", "2023-03-29");
        ps.put("convert.localtime", "20:45:01");
        ps.put("convert.localdatetime", "2023-03-29T20:45:01");
        ps.put("convert.zoneddatetime", "2023-03-29T20:45:01+08:00[Asia/Shanghai]");
        ps.put("convert.duration", "P2DT3H4M");
        ps.put("convert.zoneid", "Asia/Shanghai");

        return new PropertyResolver(ps);
    }
}
