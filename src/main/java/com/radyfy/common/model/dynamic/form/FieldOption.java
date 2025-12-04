package com.radyfy.common.model.dynamic.form;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.radyfy.common.model.dynamic.Option;

@Getter
@Setter
@Builder
public class FieldOption implements Serializable {
    private String apiUrl;
    private boolean multiple;
    private List<String> nullIds;
    private String disableCondition;
    private String showCondition;
    private List<Option> onChangeCondition;
    private String details;
    private String validate;
    private String format;
    private String addApiUrl;
    private OtpField otpField;
    private boolean add;
    private boolean delete;
    private String countTitle;
    private String selectApi;
    private String tag;
    private Object value;
    private Map<String, Object> meta;
}
