package com.radyfy.common.service.aws.sqs;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.radyfy.common.model.enums.Environment;
import com.radyfy.common.model.sqs.LoginEventSqs;
import com.radyfy.common.model.user.User;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@Service
public class SqsMessageProducer {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.queue.name}")
    private String queueName;

    public SqsMessageProducer(SqsTemplate sqsTemplate){
        this.sqsTemplate = sqsTemplate;
    }

    public void sendLoginEvent(User user, String accountId, Environment env) {
        LoginEventSqs event = new LoginEventSqs();
        event.setEmail(user.getEmail());
        event.setAccountId(accountId);
        event.setEnv(env.toString());
        event.setMessageTime(Instant.now().toString());
        sqsTemplate.send(queueName, event);
    }

    
}
