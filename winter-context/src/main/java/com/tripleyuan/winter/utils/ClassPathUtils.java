package com.tripleyuan.winter.utils;

import com.tripleyuan.winter.io.InputStreamCallback;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ClassPathUtils {

    public static <T> T readInputStream(String path, InputStreamCallback<T> inputStreamCallback) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        try (InputStream input = getContextClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new FileNotFoundException("File not found in classpath: " + path);
            }
            return inputStreamCallback.doWithInputStream(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String readString(String path) {
        return readInputStream(path, (input) -> {
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096];
                int len = 0;
                while ((len = input.read(buf)) != -1) {
                    output.write(buf, 0, len);
                }
                return new String(output.toByteArray(), StandardCharsets.UTF_8);
            }
        });
    }

    static ClassLoader getContextClassLoader() {
        ClassLoader cl = null;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassPathUtils.class.getClassLoader();
        }
        return cl;
    }
}
