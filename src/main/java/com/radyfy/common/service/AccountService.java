package com.radyfy.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.radyfy.common.commons.Constants;
import com.radyfy.common.exception.AuthException;
import com.radyfy.common.model.Account;
import com.radyfy.common.model.App;
import com.radyfy.common.model.EcomAccount;
import com.radyfy.common.model.commons.RedisData;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.service.common.MemoryService;

import java.util.Arrays;
import java.util.Collections;

@Component
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final MetaOrmDao metaOrmDao;
    private final MemoryService memoryService;

    @Autowired
    public AccountService(
            MetaOrmDao metaOrmDao,
            MemoryService memoryService
    ) {
        this.metaOrmDao = metaOrmDao;
        this.memoryService = memoryService;
    }

    public App getApp(EcomAccount ecomAccount) {
        try {
            return metaOrmDao.findOneByQuery(DaoQuery.builder().criteriaList(
                    Arrays.asList(
                            Criteria.where(Constants.ECOM_ACCOUNT_ID).is(ecomAccount.getId())
                    )
            ).build(), App.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AuthException();
        }
    }

    public EcomAccount getEcomAccount(String domain) {
        try {
            return metaOrmDao.findOneByQuery(DaoQuery.builder().criteriaList(
                    Collections.singletonList(
                            Criteria.where("domain").is(domain)
                    )
            ).build(), EcomAccount.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AuthException();
        }
    }

    public Account getAccountById(String accountId) {
        RedisData<Account> redisData = memoryService.getAccount(accountId);
        if (redisData == null) {
            Account account = metaOrmDao.getById(accountId, Account.class, null);
            memoryService.setAccount(account);
            redisData = new RedisData<Account>(account);
        }
        return redisData.getData();
    }
}
