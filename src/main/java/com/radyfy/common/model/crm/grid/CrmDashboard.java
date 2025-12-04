package com.radyfy.common.model.crm.grid;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.crm.grid.dashboard.DashboardRow;
import com.radyfy.common.model.dao.MemoryCached;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MemoryCached
@Document(collection = "crm_grid")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrmDashboard extends CrmGrid {

  private List<DashboardRow> dashboardRows;
}
