package com.radyfy.common.response;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.radyfy.common.model.dynamic.Option;

@Data

public class CheckboxGroup implements Serializable {

    private String name;
    private List<Option> options = new ArrayList<>();

    public CheckboxGroup(){
        super();
    }

    public CheckboxGroup(
            String name,
            List<Option> options
    ){
        super();
        this.name = name;
        this.options = options;
    }
}
