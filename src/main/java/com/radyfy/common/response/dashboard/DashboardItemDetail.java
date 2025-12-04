package com.radyfy.common.response.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardItemDetail {
    private String dateFilter;
    private long end;
    private long start;
    private String title;
    private List<Widget> widget;
}
