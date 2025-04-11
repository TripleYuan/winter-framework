package com.tripleyuan.winter.context;

import com.tripleyuan.winter.demo.DemoApplication;
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
