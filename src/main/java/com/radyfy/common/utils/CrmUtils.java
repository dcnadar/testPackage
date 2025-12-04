package com.radyfy.common.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;

import com.radyfy.common.model.crm.api.meta.table.DefaultFilter;
import com.radyfy.common.model.crm.grid.CrmGrid;
import com.radyfy.common.model.crm.grid.table.GridParam;
import com.radyfy.common.model.user.User;
import com.radyfy.common.request.table.ColumnFilter.FilterOperator;

public class CrmUtils {

    public static String getCrmFormIdGridKey(List<GridParam> gridParams) {
        if (Utils.isNotEmpty(gridParams)) {
            Optional<GridParam> gridParamOptional = gridParams.stream()
                    .filter(g -> "id".equals(g.getFilterKey())).findAny();
            if (gridParamOptional.isPresent()) {
                return gridParamOptional.get().getKey();
            }
            Optional<GridParam> idKeyOptional = gridParams.stream()
                    .filter(g -> "id".equals(g.getKey())
                            && (g.getDocumentKey() != null && !"id".equals(g.getDocumentKey())))
                    .findAny();
            if (idKeyOptional.isPresent()) {
                return idKeyOptional.get().getFilterKey();
            }
        }
        return "id";
    }

    public static String getFullName(User user) {
        return String.join(" ",
                Utils.isNotEmpty(user.getSalutation()) ? user.getSalutation() : "",
                Utils.isNotEmpty(user.getFirstName()) ? user.getFirstName() : "",
                Utils.isNotEmpty(user.getMiddleName()) ? user.getMiddleName() : "",
                Utils.isNotEmpty(user.getLastName()) ? user.getLastName() : "").trim();
    }

    public static String getFullName(Document userDoc) {
        return String.join(" ",
                Utils.isNotEmpty(userDoc.getString("salutation")) ? userDoc.getString("salutation") : "",
                Utils.isNotEmpty(userDoc.getString("firstName")) ? userDoc.getString("firstName") : "",
                Utils.isNotEmpty(userDoc.getString("middleName")) ? userDoc.getString("middleName") : "",
                Utils.isNotEmpty(userDoc.getString("lastName")) ? userDoc.getString("lastName") : "").trim();
    }

    // getFormalDate
    public static String getFormalDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM yyyy");
        return sdf.format(new Date());
    }

    public static <T extends CrmGrid> T cloneCrmGrid(CrmGrid crmGrid, T cloneTo) {
        cloneTo.setId(crmGrid.getId());
        cloneTo.setCreated(crmGrid.getCreated());
        cloneTo.setAccountId(crmGrid.getAccountId());
        cloneTo.setMeta(crmGrid.getMeta());
        cloneTo.setGridTitle(crmGrid.getGridTitle());
        cloneTo.setGridType(crmGrid.getGridType());
        cloneTo.setBackUrl(crmGrid.getBackUrl());
        cloneTo.setCrmModelId(crmGrid.getCrmModelId());
        cloneTo.setGridParams(crmGrid.getGridParams());
        cloneTo.setApiType(crmGrid.getApiType());
        cloneTo.setApiUrl(crmGrid.getApiUrl());
        return cloneTo;

    }

    public static List<Criteria> buildCriteriaFromDefaultFilters(List<DefaultFilter> defaultFilters) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (defaultFilters == null) {
            return criteriaList;
        }

        for (DefaultFilter defaultFilter : defaultFilters) {
            String key = defaultFilter.getPropertyKey();
            FilterOperator op = defaultFilter.getOperator();

            switch (op) {
                case EQUALS:
                    criteriaList.add(Criteria.where(key).is(defaultFilter.getEQUALS()));
                    break;
                case NOT_EQUALS:
                    criteriaList.add(Criteria.where(key).ne(defaultFilter.getNOT_EQUALS()));
                    break;
                case IN:
                    criteriaList.add(Criteria.where(key).in(defaultFilter.getIN()));
                    break;
                case EXISTS:
                    criteriaList.add(Criteria.where(key).exists(Utils.isTrue(defaultFilter.getEXISTS())));
                    break;
                default:
                    break;
            }
        }

        return criteriaList;
    }
}
