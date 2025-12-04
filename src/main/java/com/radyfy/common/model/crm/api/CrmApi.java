package com.radyfy.common.model.crm.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.BaseEntityModel;
import com.radyfy.common.model.crm.grid.table.GridParam;
import com.radyfy.common.model.dao.MemoryCached;
import com.radyfy.common.model.enums.grid.GridType;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@MemoryCached
@Document(collection = "crm_api")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrmApi extends BaseEntityModel {

    private String name;
    private String path;
    private ApiType apiType;
    private CrmApiAction action;
    private GridType gridType;
    private FileType downloadFileType;
    private Boolean getAllRecords;
    private Integer recordsLimit;

    // only for action != ENTITY_DELETE
    private String sourceId;
    // only for action == ENTITY_DELETE
    private String modelId;

    // for gridType menu
    private GridType innerGridType;
    private String innerSourceId;

    // only for action == ENTITY_DELETE or ENTITY_GET
    List<GridParam> gridParams;

    // only for action == ENTITY_GET
    private Boolean fetchDocWithoutId;

    // private List<EcomApiMapping> ecomApiMappings;
    

    public enum CrmApiAction{
        CRM_GRID("Send Crm Grid"),
        FORM_SAVE("Save form data"),
        ENTITY_DELETE("Delete Entity"),
        ENTITY_GET("Get Entity"),
        FORM_VALUES_OPTIONS("Get form values options"),
        FORM_FIELDS("Get form fields"),
        DOWNLOAD_FILE("Download file");

        private String name;
        CrmApiAction(String name){
            this.name = name;
        }

        public String value(){
            return this.name;
        }
    }

    public enum FileType{
        EXCEL("excel"),
        PDF("pdf");

        private String name;
        FileType(String name){
            this.name = name;
        }

        public String value(){
            return this.name;
        }
    }
}
