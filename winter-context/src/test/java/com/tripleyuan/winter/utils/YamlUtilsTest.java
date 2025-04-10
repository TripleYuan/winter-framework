package com.tripleyuan.winter.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlUtilsTest {

    @Test
    public void loadYaml() {
        Map<String, Object> map = YamlUtils.loadYaml("/test-config.yaml");

        assertThat(map).isNotEmpty();
        assertThat(map.containsKey("mode")).isTrue();
        assertThat(map.get("mode")).isEqualTo("winter-framework");

        assertThat(map.containsKey("user")).isTrue();
        Map<String, Object> user = (Map<String, Object>) map.get("user");
        assertThat(user.containsKey("name")).isTrue();
        assertThat(user.get("name")).isEqualTo("admin");
        assertThat(user.containsKey("password")).isTrue();
        assertThat(user.get("password")).isEqualTo("admin");
    }
}
