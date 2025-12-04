package com.radyfy.common.model.crm.grid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.dao.MemoryCached;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.model.dynamic.form.FormGroup;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.model.enums.grid.ActionGridType;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@MemoryCached
@Document(collection = "crm_grid")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrmForm extends CrmGrid{

        private Boolean edit = true;

        // add is for form inside dynamic table, to add rows
        // private Boolean add = false;
        // private String param;
        private FormGroup[] rows;
        private ActionGridType submitType;
        private String responseType;
        private List<Button> actions;
        private List<Option> tabs;
        private Boolean fetchDocWithoutId;
        private Boolean upsertDoc;
        private String postUrl;
}
