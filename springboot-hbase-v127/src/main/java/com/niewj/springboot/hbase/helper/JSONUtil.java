package com.niewj.springboot.hbase.helper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JSONUtil {
    private static Logger logger = LoggerFactory.getLogger(JSONUtil.class);
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    public static String toJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T read(String json, Class<T> type) throws IOException {
        return objectMapper.readValue(json.getBytes(Charsets.UTF_8), type);
    }

    /**
     * 将JsonStr转换成类似List<T>这种类型，使用方式：
     * JSONUtil.read(jsonStr, new TypeReference<List<T>>(){})
     */
    public static <T> T read(String json, TypeReference<T> reference) throws IOException {
        return objectMapper.readValue(json.getBytes(Charsets.UTF_8), reference);
    }

    public static <T> T read(byte[] json, Class<T> type) throws IOException {
        return objectMapper.readValue(json, type);
    }

    public static String safeToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            logger.error("Exception->\n {}", e);
            throw Throwables.propagate(e);
        }
    }

    public static byte[] safeToByte(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (IOException e) {
            logger.error("Exception->\n {}", e);
            throw Throwables.propagate(e);
        }
    }

    public static <T> T safeRead(String json, Class<T> type) {
        try {
            return read(json, type);
        } catch (IOException e) {
            logger.error("Exception->\n {}", e);
            throw Throwables.propagate(e);
        }
    }

    public static <T> List<T> safeReadList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            logger.error("Exception->\n {}", e);
            throw Throwables.propagate(e);
        }
    }


    public static <T> T read(InputStream in, Class<T> type) throws IOException {
        return objectMapper.readValue(in, type);
    }


    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
