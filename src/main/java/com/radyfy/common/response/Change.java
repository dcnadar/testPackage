package com.radyfy.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Change {
    private String name;
    private String oldValue;
    private String newValue;
}
