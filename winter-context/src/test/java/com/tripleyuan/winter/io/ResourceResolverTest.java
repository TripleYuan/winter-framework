package com.tripleyuan.winter.io;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceResolverTest {

    @Test
    public void scan() {
        ResourceResolver resolver = new ResourceResolver("com.tripleyuan.winter.io");
        List<String> list = resolver.scan(r -> {
            if (r.getName().endsWith(".class")) {
                return r.getName();
            }
            return null;
        });
        assertThat(list).isNotEmpty();
        list.forEach(s -> assertThat(s.endsWith(".class")).isTrue());
    }
}
