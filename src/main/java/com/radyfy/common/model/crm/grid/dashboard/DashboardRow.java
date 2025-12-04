package com.radyfy.common.model.crm.grid.dashboard;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardRow implements Serializable{

	private String rowLabel;
	private List<DashboardPage> page;
}
