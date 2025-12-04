package com.radyfy.common.model.crm.grid.menu;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.utils.BsonDocumentUtils;
import com.radyfy.common.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CrmMenuTargets implements Serializable {
    private String thumb;
    private String title;
    private String description;
    private String slug;
    private List<Option> details;

    public CrmMenuTargets generate(Document obj) {
        CrmMenuTargets menuData = new CrmMenuTargets();

        if (Utils.isNotEmpty(title)) {
            menuData.setTitle(BsonDocumentUtils.getDataValueAsString(obj, title));
        }

        if (Utils.isNotEmpty(description)) {
            menuData.setDescription(BsonDocumentUtils.getDataValueAsString(obj, description));
        }

        if (Utils.isNotEmpty(thumb)) {
            menuData.setThumb(BsonDocumentUtils.getDataValueAsString(obj, thumb));
        }

        if (Utils.isNotEmpty(slug)) {
            menuData.setSlug(slug);
        }

        if (Utils.isNotEmpty(details)) {
            List<Option> details = new ArrayList<>();
            for (Option option : this.details) {
                String value = BsonDocumentUtils.getDataValueAsString(obj, option.getValue().toString());
                if (Utils.isNotEmpty(value)) {
                    Option newOption = new Option();
                    newOption.setKey(option.getKey());
                    newOption.setValue(value);
                    details.add(newOption);
                }
            }
            menuData.setDetails(details);
        }

        return menuData;
    }

    public List<String> getFetchableFields() {
        List<String> fields = new ArrayList<>();

        if (Utils.isNotEmpty(title)) {
            fields.add(title);
        }

        if (Utils.isNotEmpty(thumb)) {
            fields.add(thumb);
        }

        if (Utils.isNotEmpty(description)) {
            fields.add(description);
        }

        if (Utils.isNotEmpty(details)) {
            for (Option option : details) {
                fields.add(option.getValue().toString());
            }
        }

        return fields;
    }
}
