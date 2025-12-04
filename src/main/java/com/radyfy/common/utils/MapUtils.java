package com.radyfy.common.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {

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

    public static Object getDataValue(Map<String, Object> data, String key) {
        if(data == null){
            return null;
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
                return data.get(name);
            } else {
                Object map = data.get(name);
                if(map == null){
                    return null;
                }
                data = (Map<String, Object>) map;
            }
        }
        return null;
    }

    public static <T> List<T> getListValue(Map<String, Object> data, String key) {
        return (List<T>) getDataValue(data, key);
    }
}
