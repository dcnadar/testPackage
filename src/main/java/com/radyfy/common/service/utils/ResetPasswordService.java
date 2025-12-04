package com.radyfy.common.service.utils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.radyfy.common.auth.PasswordHash;
import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.commons.Errors;
import com.radyfy.common.model.EcomAccount;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.model.enums.UserStatus;
import com.radyfy.common.request.ResetPasswordRequest;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.EntityOrmDao;
import com.radyfy.common.service.email.SendGridService;

@Service
public class ResetPasswordService {

    @Value("${app.admin.fromEmail}")
    private String FROM_EMAIL;

    @Value("${app.sendgrid.reset_password_template_id}")
    private String SENDGRID_RESET_PASSWORD_TEMPLATE_ID;

    private final CrmModelService crmModelService;
    private final EntityOrmDao entityOrmDao;
    private final PasswordHash passwordHash;
    private final CurrentUserSession currentUserSession;
    private final SendGridService sendGridService;

    @Autowired
    public ResetPasswordService(PasswordHash passwordHash,
             CurrentUserSession currentUserSession, EntityOrmDao entityOrmDao,
            SendGridService sendGridService,
            CrmModelService crmModelService) {
        this.passwordHash = passwordHash;
        this.currentUserSession = currentUserSession;
        this.sendGridService = sendGridService;

        this.entityOrmDao = entityOrmDao;
        this.crmModelService = crmModelService;
    }

    public Document sendUserForgetEmail(String email) {

        Document forgetEmailRequest = createForgetEmailDoc(email);
        return forgetEmailRequest;
        

    }

    public void sendForgetEmail(String toEmail) {

        Document forgetEmailRequest = createForgetEmailDoc(toEmail);

        EcomAccount ecomAccount = currentUserSession.getEcomAccount();

        String primaryDomain = ecomAccount.getDomain().get(0);
        String forgetDomain = "https://" + primaryDomain;
        String forgetEmailLink = forgetDomain + "/admin/reset-password?requestId="
                + forgetEmailRequest.get("requestId") + "&email=" + toEmail;

        // Load HTML template from file and replace placeholders
        String accountName = ecomAccount.getName() != null ? ecomAccount.getName() : "Radyfy";
        String logoUrl = ecomAccount.getLogo() != null ? ecomAccount.getLogo() : "";

        Map<String, Object> dynamicData = new HashMap<>();

        dynamicData.put("accountName", accountName);
        dynamicData.put("resetLink", forgetEmailLink);
        dynamicData.put("logoUrl", logoUrl);
        dynamicData.put("currentYear", String.valueOf(java.time.Year.now().getValue()));
        sendGridService.sendEmailAsync(FROM_EMAIL, toEmail, SENDGRID_RESET_PASSWORD_TEMPLATE_ID, dynamicData);
    }

    public void verifyAndResetPassword(ResetPasswordRequest resetRequest) {

        if (!resetRequest.getNewPassword().equals(resetRequest.getConfirmPassword())) {
            throw new RuntimeException(Errors.PASSWORD_MISMATCH);
        }

        DaoQuery daoQuery = DaoQuery.fromCriteria(Criteria.where("email").is(resetRequest.getEmail()));
        daoQuery.setSort(Sort.by(Sort.Order.desc("created")));
        daoQuery.setLimit(1);

        CrmModel forgetPasswordModel = crmModelService
        .getModelByCollectionName(CollectionNames.RESET_PASSWORD_REQUEST);

        List<Document> forgetEmailRequests = entityOrmDao.findByQuery(daoQuery,
                forgetPasswordModel);
        if (forgetEmailRequests.isEmpty()) {
            throw new RuntimeException(Errors.INVALID_REQUEST);
        }

        Document forgetEmailRequest = forgetEmailRequests.get(0);
        if (!forgetEmailRequest.get("requestId").equals(resetRequest.getRequestId())) {
            throw new RuntimeException(Errors.INVALID_REQUEST);
        }

        if (forgetEmailRequest.getBoolean("isUsed")) {
            throw new RuntimeException(Errors.LINK_USED);
        }

        long expirationTime = 10 * 60 * 1000;
        if (System.currentTimeMillis() - forgetEmailRequest.getDate("created").getTime() > expirationTime) {
            throw new RuntimeException(Errors.EXPIRED_REQUEST);
        }

        String hashPassword = passwordHash.hashPassword(resetRequest.getNewPassword());

        CrmModel userModel = crmModelService.getModelByCollectionName(CollectionNames.user);

        Update update = new Update();
        update.set("password", hashPassword);
        update.set("isFirstLogin", false);
        update.set("status", UserStatus.ACTIVE.toString());
        daoQuery = DaoQuery.fromCriteria(Criteria.where("email").is(resetRequest.getEmail()));
        entityOrmDao.updateByQuery(daoQuery, update, userModel);

        update = new Update();
        update.set("isUsed", true);
        daoQuery = DaoQuery.fromCriteria(Criteria.where("_id").is(new ObjectId(forgetEmailRequest.getString("_id"))));
        entityOrmDao.updateByQuery(daoQuery, update, forgetPasswordModel);
    }

    private Document createForgetEmailDoc(String email) {
        DaoQuery daoQuery = DaoQuery.fromCriteria(
                Criteria.where("email").is(email)
                .and("status").in(UserStatus.ACTIVE.toString(), UserStatus.INVITED.toString()));

        CrmModel userModel = crmModelService.getModelByCollectionName(CollectionNames.user);

        if (entityOrmDao.getCount(daoQuery, userModel) == 0) {
            throw new RuntimeException(Errors.USER_NOT_REGISTERED);
        }

        long fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000;
        daoQuery = DaoQuery.builder().criteriaList(
                Arrays.asList(
                        Criteria.where("email").is(email),
                        Criteria.where("created").gt(new Date(fiveMinutesAgo))))
                .build();

        CrmModel forgetPasswordModel = crmModelService
                .getModelByCollectionName(CollectionNames.RESET_PASSWORD_REQUEST);

        if (entityOrmDao.getCount(daoQuery, forgetPasswordModel) > 0) {
            throw new RuntimeException(Errors.REQEUST_SENT_RECENTLY);
        }

        Document forgetPasswordDoc = new Document();
        forgetPasswordDoc.put("email", email);
        forgetPasswordDoc.put("requestId", UUID.randomUUID().toString());
        forgetPasswordDoc.put("isUsed", false);

        return entityOrmDao.create(forgetPasswordDoc, forgetPasswordModel);

    }

}
