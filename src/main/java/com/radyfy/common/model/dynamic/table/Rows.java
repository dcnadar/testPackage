package com.radyfy.common.model.dynamic.table;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Rows<T> {

    private String name;
    private List<Button> actions;
    private List<T> data;
}
