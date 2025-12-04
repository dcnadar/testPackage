package com.radyfy.common.model.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ActionResponse {
    private String responseMessage;
    private String redirectSlug;
}
