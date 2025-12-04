package com.radyfy.common.service.common;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.radyfy.common.model.Account;
import com.radyfy.common.model.commons.RedisData;
import com.radyfy.common.model.crm.model.BaseCrmModel;
import com.radyfy.common.model.dao.MemoryCached;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MemoryService {

    private static final Logger logger = LoggerFactory.getLogger(MemoryService.class);

    private final Map<String, RedisData<?>> memory;
    public Map<String, RedisData<List<BaseCrmModel>>> accountBaseCrmModels;
    public Map<String, RedisData<Account>> accounts;

    public MemoryService() {
        this.memory = new ConcurrentHashMap<>();
        this.accountBaseCrmModels = new ConcurrentHashMap<>();
        this.accounts = new ConcurrentHashMap<>();
    }

    public void saveData(String key, RedisData<?> data, Class<?> klass) {
        if(klass.getAnnotation(MemoryCached.class) != null) {
            memory.put(key, SerializationUtils.clone(data));
            logger.debug("cache saved for key {}, total: {}", key, memory.size());
        } else {
            logger.debug("cache data not saved for class {}, is_list: {}", klass, data instanceof List);
        }
    }

    public RedisData<?> getData(String key) {
        return SerializationUtils.clone(memory.get(key));
    }

    public void invalidateData(String key) {
        memory.remove(key);
    }

    public RedisData<Account> getAccount(String accountId) {
        return SerializationUtils.clone(this.accounts.get(accountId));
    }

    public void setAccount(Account account){
        this.accounts.put(account.getId(), new RedisData<>(account));
    }

    public RedisData<List<BaseCrmModel>> getBaseCrmModels(String accountId){
        return SerializationUtils.clone(this.accountBaseCrmModels.get(accountId));
    }

    public void setBaseCrmModels(String accountId, List<BaseCrmModel> baseCrmModels){
        this.accountBaseCrmModels.put(accountId, new RedisData<>(baseCrmModels));
    }

    public void invalidateAll() {
        memory.clear();
        accountBaseCrmModels.clear();
        accounts.clear();
    }
}

