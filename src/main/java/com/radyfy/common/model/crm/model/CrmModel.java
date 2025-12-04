package com.radyfy.common.model.crm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.crm.grid.menu.CrmMenuTargets;
import com.radyfy.common.model.dao.MemoryCached;
import com.radyfy.common.model.dynamic.card.CardData;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@MemoryCached
@Document(collection = "crm_model")
@CompoundIndex(name = "unique-name-ecom", def = "{'accountId': 1, 'name': 1, 'ecomAccountId': 1}", unique = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrmModel extends CrmBasicModel {

    // collection or inner type
    private List<ModelProperty> properties;
    private String baseModelId;

    // collection
    // private List<CrmModelIndex> indexes;
    private String collectionName;
    private CardData cardDataTargets;
    private CrmMenuTargets menuTargets;
    // TODO create proper stucture for indexes, this is temp
    private List<List<String>> compoundUniqueFields;

    private Boolean isOrg;
    private Boolean isOrgScopeApplicable;
    private String parent;
    private String parentRelationship;
    private Boolean isUserAccount;

    

    public CrmModel() {
        super();
    }

    public CrmModel(String collectionName, String baseModelId) {
        super();
        this.collectionName = collectionName;
        this.baseModelId = baseModelId;
    }
}
