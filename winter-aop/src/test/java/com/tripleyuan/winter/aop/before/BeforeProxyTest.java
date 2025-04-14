package com.tripleyuan.winter.aop.before;

import com.tripleyuan.winter.context.AnnotationConfigApplicationContext;
import com.tripleyuan.winter.io.PropertyResolver;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class BeforeProxyTest {

    @Test
    public void testBeforeProxy() {
        try (var context = new AnnotationConfigApplicationContext(BeforeApplication.class, createPropertyResolver())) {
            BusinessBean proxy = context.getBean(BusinessBean.class);
            System.out.println(proxy);

            assertNotSame(proxy.getClass(), BusinessBean.class);
            assertEquals("Hello, Bob.", proxy.hello("Bob"));
            assertEquals("Morning, Alice.", proxy.morning("Alice"));
        }
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("customer.name", "Bob");
        var pr = new PropertyResolver(ps);
        return pr;
    }
}
