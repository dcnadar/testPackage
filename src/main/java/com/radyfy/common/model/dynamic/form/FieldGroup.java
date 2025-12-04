package com.radyfy.common.model.dynamic.form;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldGroup {

    String value();
    int index();
    boolean update() default true;
    String tab() default "";
}
