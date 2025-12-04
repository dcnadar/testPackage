package com.radyfy.common.model.dynamic;

import org.bson.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocCreateResult {
    private final Document document;
    private final Object returnValue;
}
