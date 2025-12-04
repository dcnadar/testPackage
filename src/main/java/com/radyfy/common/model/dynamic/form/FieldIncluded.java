package com.radyfy.common.model.dynamic.form;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldIncluded {
    int value() default -1;
}
