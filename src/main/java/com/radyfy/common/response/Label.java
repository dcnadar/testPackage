package com.radyfy.common.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Label implements Serializable {

    private String symbol;
    private String name;
    private String color;
    private double value;
    private boolean privateLabel;
}
