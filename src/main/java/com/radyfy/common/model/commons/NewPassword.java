package com.radyfy.common.model.commons;

import com.radyfy.common.model.dynamic.form.FormItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPassword {

    @FormItem(value = "Password", index = 0, type = FormItem.Type.password)
    private String password;
}
