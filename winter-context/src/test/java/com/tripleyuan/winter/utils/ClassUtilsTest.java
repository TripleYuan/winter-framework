package com.tripleyuan.winter.utils;

import com.tripleyuan.winter.annotation.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassUtilsTest {

    @Test
    void findAnnotation() {
        assertThat(ClassUtils.findAnnotation(Apple.class, Component.class)).isNotNull();
        assertThat(ClassUtils.findAnnotation(Banana.class, Component.class)).isNotNull();
    }


    @Component
    static class Apple {

    }

    @Service("Banana")
    static class Banana {

    }
}
