package com.yurupari.calendar.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JsonTestUtils {

    private final JsonMapper jsonMapper;

    public String loadRequest(String filePath) throws IOException {
        var resource = new ClassPathResource(filePath);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public <T> T loadObject(String filePath, Class<T> targetClass) throws IOException {
        var resource = new ClassPathResource(filePath);
        return jsonMapper.readValue(resource.getInputStream(), targetClass);
    }
}
