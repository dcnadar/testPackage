package com.radyfy.common.model.crm.grid.dashboard;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.enums.grid.GridType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardPage implements Serializable{
	private String title;
	private GridType pageType;
	private String page;
	private Long width;
	private Long height;
	private String bgColor;
}
