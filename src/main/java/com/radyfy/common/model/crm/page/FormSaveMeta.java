package com.radyfy.common.model.crm.page;

import org.bson.Document;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormSaveMeta {

    private String pageId;
    private GridRequestParams params;
    private Document document;



    public FormSaveMeta(String pageId, Document document, GridRequestParams params) {
        this.pageId = pageId;
        this.document = document;
        if (params == null) {
            this.params = new GridRequestParams();
        } else {
            this.params = params;
        }
    }

}
