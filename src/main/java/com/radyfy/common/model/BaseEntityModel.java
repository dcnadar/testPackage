package com.radyfy.common.model;

import lombok.Getter;
import lombok.Setter;

import org.bson.Document;
import org.springframework.data.annotation.Id;

import com.radyfy.common.utils.BsonDocumentUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class BaseEntityModel implements Serializable {

    @Id
    private String id;
    private String accountId;

    private Date created;
    private Date updated;

    // for dynamic data
    private Map<String, Object> meta;

    public void setMetaValue(String key, Object value) {

        if(meta == null){
            meta = new HashMap<>();
        }
        Map<String, Object> data = meta;
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

    public Object getDataValue(String key) {
        if(meta == null){
            return null;
        }
        Map<String, Object> data = meta;
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
}
