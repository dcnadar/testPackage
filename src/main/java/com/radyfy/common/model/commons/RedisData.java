package com.radyfy.common.model.commons;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RedisData<T> implements Serializable {
    private T data;

    public RedisData(){
        super();
    }
    public RedisData(T data){
        this.data = data;
    }
}
