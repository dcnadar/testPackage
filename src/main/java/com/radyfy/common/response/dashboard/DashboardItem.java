package com.radyfy.common.response.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardItem {
    private int span;
    private Boolean fullScreen;
    private String key;
    private boolean open;
    private int order;
    private String url;
    private String value;
}
