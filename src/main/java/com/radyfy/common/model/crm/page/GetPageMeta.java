package com.radyfy.common.model.crm.page;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radyfy.common.model.commons.BreadcrumbItem;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.utils.Utils;
import com.radyfy.common.utils.ValidationUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetPageMeta {

    private String pageId;
    private String slug;
    private String parentSlug;
    private GridRequestParams params;
    private List<BreadcrumbItem> breadcrumb;
    private String payload;

    public GetPageMeta(String pageId, String slug, GridRequestParams params, String payload) {
        setInit(pageId, slug, params, payload);
    }

    public GetPageMeta(String pageId, String parentSlug, String slug, String breadcrumb,
            GridRequestParams params, String payload) {
        if (Utils.isNotEmpty(parentSlug) && Utils.isNotEmpty(breadcrumb)) {
            this.parentSlug = parentSlug;
            try {
                ObjectMapper objectMapper = new ObjectMapper();

                this.breadcrumb = objectMapper.readValue(breadcrumb,
                        new TypeReference<List<BreadcrumbItem>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to decode breadcrumb: " + e.getMessage());
            }
        }
        setInit(pageId, slug, params, payload);
    }

    private void setInit(String pageId, String slug, GridRequestParams params, String payload) {
        if (ValidationUtils.isValidHexID(pageId)) {
            this.pageId = pageId;
        } else {
            throw new RuntimeException("Invalid pageId");
        }
        this.slug = slug;
        if (params == null) {
            this.params = new GridRequestParams();
        } else {
            this.params = params;
        }
        this.payload = payload;
    }

}
