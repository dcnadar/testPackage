package com.radyfy.common.model.dynamic.form;

import java.lang.annotation.*;

@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD,
        ElementType.TYPE, ElementType.PARAMETER})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface FormItem {

    String value();
    String key() default "";
    int index();
    Type type() default Type.text;
    boolean optional() default false;
    int min() default 0;
    int max() default 100;
    int span() default 8;
    int group() default 0;
    Visibility updateType() default Visibility.none;
    // only for String fields and only works when optional false
    boolean unique() default false;
    Class<? extends FieldOptionProvider> properties() default FieldOptionProvider.class;

    enum Type {
        text, password, textarea, number, email, list, date, bool, url, url_path,
        image, upload, checkbox_group, date_range, form_table, date_time,
        location, radio, input_table, time, webcam, otp,
        form_array, search, tag, card, tree, date_range_2, next_date, text_editor,
        color, attachment, inner_form, strings, input_group,
        notion_text_editor, json, upload_v2, upload_icon,code_editor,

        // custom
        c_page_section_selection, radyfy_dasboard_builder
    }

    enum Visibility {
        none, hidden, hidden_fe, disabled, disabled_fe;
    }
}
