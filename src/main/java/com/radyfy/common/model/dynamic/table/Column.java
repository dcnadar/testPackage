package com.radyfy.common.model.dynamic.table;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD,
        ElementType.TYPE, ElementType.PARAMETER})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Column{

    String value();
    String key() default "";
    int index();
    boolean sort() default true;
    boolean show() default true;
    boolean select() default true;
    Type type() default Type.text;
    int width() default 0;
    Class<? extends TableCellProvider> properties() default TableCellProvider.class;

    enum Type {
        text, name, link, actions, date, date_time, mark, options, errors, changes, tag, input, card,
        thumb, checkbox_group, html_view, mdx_view,

        // form table
        list
    }

}