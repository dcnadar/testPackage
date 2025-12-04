package com.radyfy.common.model.dynamic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.radyfy.common.model.dynamic.form.FormGroup;
import com.radyfy.common.model.dynamic.table.TableColumn;
import com.radyfy.common.response.CheckboxGroup;

@Getter
@Setter
public class Matrix {

    private List<String> fields = new ArrayList<>();
    private List<TableColumn> columns = new ArrayList<>();
    private FormGroup[] formGroups;
    private CheckboxGroup[] exportFields;
}
