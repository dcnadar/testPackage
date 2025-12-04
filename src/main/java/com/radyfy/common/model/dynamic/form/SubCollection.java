package com.radyfy.common.model.dynamic.form;

import java.lang.annotation.*;

import com.radyfy.common.model.BaseEntityModel;

@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD,
        ElementType.TYPE, ElementType.PARAMETER})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCollection {
    String absentValue() default "";
    String[] keys() default {};
    Class<? extends BaseEntityModel> value() default BaseEntityModel.class;
}
