package com.radyfy.common.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document("account_tag")
public class AccountTag extends BaseEntityModel {

    private List<String> name;
    private String category;
    private String categoryName;

}
