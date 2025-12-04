package com.radyfy.common.response;

import lombok.Data;

import java.util.Map;

@Data
public class TableDetails {

    private Progress progress;
    private Map<String, Label> labels;
}
