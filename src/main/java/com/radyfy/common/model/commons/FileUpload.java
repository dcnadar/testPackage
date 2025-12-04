package com.radyfy.common.model.commons;

import com.radyfy.common.model.dynamic.form.FormItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUpload {

    @FormItem(value = "Upload file (xlsx)", type = FormItem.Type.upload, index = 0, span = 24)
    private String file;
}
