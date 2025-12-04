package com.radyfy.common.model.crm.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItem implements Serializable {

    private Boolean isSubmenu;
    private String key;
    private String slug;
    private String page;
    private String value;
    private String icon;
    private List<MenuItem> subMenu;

}
