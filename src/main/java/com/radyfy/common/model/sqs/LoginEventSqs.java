package com.radyfy.common.model.sqs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginEventSqs {
    private String email;
    private String accountId;
    private String env;
    private String messageTime;
}

