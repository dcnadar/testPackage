package com.radyfy.common.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BsonDocumentUtils {
    public static void setDataValue(Map<String, Object> data, String key, Object value) {
        if(data == null){
            throw new RuntimeException("Data cannot be null!");
        }
        String[] keys;
        if (key.contains(".")) {
            keys = key.split("\\.");
        } else {
            keys = new String[]{key};
        }

        for (int i = 0; i < keys.length; i++) {
            String name = keys[i];
            if (i == keys.length - 1) {
                data.put(name, value);
            } else {
                Object map = data.computeIfAbsent(name, k -> new HashMap<>());
                data = (Map<String, Object>) map;
            }
        }
    }

    public static String getDataValueAsString(Map<String, Object> data, String key) {
        Object value = getDataValue(data, key);
        if(value instanceof String)
            return (String) value;
        return null;
    }

    public static Date getDataValueAsDate(Map<String, Object> data, String key) {
        return Utils.parseDate(getDataValue(data, key));
    }

    public static Object getDataValue(Map<String, Object> data, String key){
        return getDataValue(data, key, null);
    }
    public static Object getDataValue(Map<String, Object> data, String key, Object defaultValue) {
        if(data == null){
            return defaultValue;
        }
        String[] keys;
        if (key.contains(".")) {
            keys = key.split("\\.");
        } else {
            keys = new String[]{key};
        }

        for (int i = 0; i < keys.length; i++) {
            String name = keys[i];
            if (i == keys.length - 1) {
                Object value = data.get(name);
                if(value == null){
                    return defaultValue;
                }
                return value;
            } else {
                Object obj = data.get(name);
                if(obj == null){
                    return defaultValue;
                }
                data = (Map<String, Object>) obj;
            }
        }
        return defaultValue;
    }

    public static boolean removeField(Map<String, Object> data, String key) {
        if(data == null){
            return false;
        }
        String[] keys;
        if (key.contains(".")) {
            keys = key.split("\\.");
        } else {
            keys = new String[]{key};
        }

        for (int i = 0; i < keys.length; i++) {
            String name = keys[i];
            if (i == keys.length - 1) {
                return data.remove(name) != null;
            } else {
                Object obj = data.get(name);
                if(obj == null || !(obj instanceof Map)){
                    return false;
                }
                data = (Map<String, Object>) obj;
            }
        }
        return false;
    }
}
