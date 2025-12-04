package com.radyfy.common.model.dynamic.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.model.dynamic.table.TableColumn;
import com.radyfy.common.response.CheckboxGroup;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormField implements Serializable {

    @Field("id")
    private String id;
    private List<Option> options;
    private Integer span;
    private Integer min;
    private Integer max;
    private String title;
    private FormItem.Type type;
    private String fileAccept;
    private Object value;
    private Integer index;
    private String placeholder;
    private Boolean o;
    private String apiUrl;
    private List<String> nullIds;
    private String disableCondition;
    private String showCondition;
    private List<Option> onChangeCondition;
    private Boolean multiple;
    // only for input_table's footer
    private String details;
    private String validate;
    private String format;
    private String hint;
    private CheckboxGroup[] checkboxGroups;
    private FormItem.Visibility updateType;
    private FormItem.Visibility createType;

    // form_table, input_table, not from db
    private List<TableColumn> columns;
    // form_table
    private FormGroup[] formRows;

    // search field
    private String selectApi;

    // select field
    private String addApiUrl;


    // input table, form array
    private Boolean add;
    private Boolean delete;
    // form array
    private String countTitle;
    // form table, input table, form array
    private OtpField otpField;
    private String tag;
    private List<String> textEditorElements;

    /**
     * Addition Support with meta fields
     * 
     * addonAfter, addonBefore, for type text, number, mobile
     * horizontal, for type radio, checkbox
     * labelCondition for any field
     * crmFormId, crmFormIdField for form_table, input_table, form_array and inner_form
     * defaultOpen, showCopyToClipboard for inner_form
     * modelUniqueKey, queryParams, searchAdditionalKeys for type list
     * inputParams for input_group
     * uploadUISize for upload
     * */
    private Map<String, Object> meta;
    private String codeExample;

}
