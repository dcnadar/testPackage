package com.radyfy.common.response.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.radyfy.common.response.dashboard.data.WidgetData;

@Getter
@Setter
public class Widget {

    public enum Type{
        LIST, ACTIONS, THUMB, CARD
    }
    private Type type;
    private int span;
    private List<WidgetData> data;
}
