package com.radyfy.common.model.dynamic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Searchable {

    // highest but not included in search
    boolean thumbColor() default false;

    // highest and included in search
    boolean thumbInitials() default false;

    // highest but not included in search
    boolean thumb() default false;

    // medium and included in search, value used for description
    boolean description() default false;
    // last and included in search, value used for title
    boolean value() default true;

    /**
     *  primary are used for unique key available for model to search (used in export/import)
     */
    boolean primary() default false;
}
