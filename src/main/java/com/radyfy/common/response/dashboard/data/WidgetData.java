package com.radyfy.common.response.dashboard.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class WidgetData implements Serializable {
    private String title;
    private int span = 12;
}
