package com.radyfy.common.service.email;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SendGridService {
    @Value("${app.sendgrid.api_key}")
    private String SENDGRID_API_KEY;

    @Async
    public CompletableFuture<Void> sendEmailAsync(String fromEmail, String toEmail, String templateId, Map<String, Object> dynamicData) {
        try{
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        // Add dynamic data to personalization
        if (dynamicData != null) {
            for (Map.Entry<String, Object> entry : dynamicData.entrySet()) {
                personalization.addDynamicTemplateData(entry.getKey(), entry.getValue());
            }
        }
        mail.addPersonalization(personalization);
        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            log.error("SendGrid async email failed: " + ex.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}
