package com.radyfy.common.model.dynamic.form;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OtpField implements Serializable {
    private String requestApi;
    private String verifyApi;
    private String resendApi;
    private String requestTitle;
    private String completeTitle;
    private Boolean completed;
    private int resendTime;
    private int length;
}
