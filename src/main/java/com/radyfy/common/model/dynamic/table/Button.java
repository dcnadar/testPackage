package com.radyfy.common.model.dynamic.table;

import lombok.*;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Transient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.radyfy.common.model.crm.api.ApiType;
import com.radyfy.common.model.enums.grid.ActionGridType;
import com.radyfy.common.model.enums.grid.GridType;
import com.radyfy.common.response.dashboard.data.WidgetData;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Button extends WidgetData {

    private ButtonInitType initType;

    private String entity;
    private String page;

    //page
    private PageOpenType pageOpenType;
    private String pageKey;
    @Transient
    private List<PageParam> pageParams;
    //form
    private FetchFormDataby fetchFormDataby;

    @Transient
    private GridType pageType;
    
    // action
    private ActionConfirmUI actionConfirmUI;
    private ActionType actionType;
    private String entityId;

    private String gridTitle;
    private ActionGridType gridType;
    private String gridMsg;
    private String gridMsgParams;
    private String apiUrl;
    private String slug;
    private String value;
    // if type is null then UI will show button
    private ButtonType type;
    private ButtonStyle btnStyle;
    private String icon;
    private String showCondition;
    private Boolean showGridComment;
    private String responseType;
    private String color;
    private Boolean pagination;
    private ApiType apiType;
    private Map<String, Object> meta;
    private String submitType;
    private String permissions;
    private String slugTarget;
    private String svg;
    private String iconPosition;
}
