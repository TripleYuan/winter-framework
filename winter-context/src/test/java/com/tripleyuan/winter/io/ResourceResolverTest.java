package com.tripleyuan.winter.io;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.sql.DataSourceDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tripleyuan.winter.io.ResourceResolver.FULL_CLASS_NAME_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

public class ResourceResolverTest {

    @Test
    public void scan_classes() {
        ResourceResolver resolver = new ResourceResolver("com.tripleyuan.winter.io");
        List<String> list = resolver.scan(FULL_CLASS_NAME_MAPPER);
        assertThat(list).isNotEmpty();
        list.forEach(s -> {
            System.out.println(s);
            assertThat(s.startsWith("com.tripleyuan.winter.io")).isTrue();
        });
    }

    @Test
    public void scan_jar() {
        String pkg = PostConstruct.class.getPackage().getName();
        ResourceResolver resolver = new ResourceResolver(pkg);
        List<String> classes = resolver.scan(FULL_CLASS_NAME_MAPPER);
        // classes in jar:
        assertThat(classes.contains(PostConstruct.class.getName())).isTrue();
        assertThat(classes.contains(PreDestroy.class.getName())).isTrue();
        assertThat(classes.contains(PermitAll.class.getName())).isTrue();
        assertThat(classes.contains(DataSourceDefinition.class.getName())).isTrue();
    }
}
