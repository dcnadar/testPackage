package com.radyfy.common.service.user;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.radyfy.common.model.EcomAccount;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.email.SendGridService;

@Service
public class UserEmailService {

  @Value("${app.admin.fromEmail}")
  private String FROM_EMAIL;
  @Value("${app.sendgrid.user_welcome_template_id}")
  private String SENDGRID_USER_WELCOME_TEMPLATE_ID;

  private final SendGridService sendGridService;
  private final CurrentUserSession currentUserSession;


  public UserEmailService(SendGridService sendGridService, CurrentUserSession currentUserSession) {
    this.sendGridService = sendGridService;
    this.currentUserSession = currentUserSession;
  }


  public void sendUserWelcomeEmail(String name, String toEmail, String password) {

    EcomAccount ecomAccount = currentUserSession.getEcomAccount();

    String accountName = ecomAccount.getName();
    String logoUrl = ecomAccount.getLogo();
    String domain = ecomAccount.getDomain().get(0);

    String loginUrl = "https://" + domain + "/login";

    Document dynamicData = new Document();

    dynamicData.put("name", name);
    dynamicData.put("email", toEmail);
    dynamicData.put("password", password);
    dynamicData.put("accountName", accountName);
    dynamicData.put("logoUrl", logoUrl);
    dynamicData.put("loginUrl", loginUrl);

    sendGridService.sendEmailAsync(FROM_EMAIL, toEmail, SENDGRID_USER_WELCOME_TEMPLATE_ID, dynamicData);

}
  
}
