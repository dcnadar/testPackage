package com.radyfy.common.response.dashboard.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListData extends WidgetData{
    private String icon;
    private String value;
    private String iconTooltip;
}
