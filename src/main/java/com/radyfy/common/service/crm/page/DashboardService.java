package com.radyfy.common.service.crm.page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.model.crm.grid.CrmDashboard;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.service.MetaOrmDao;

@Component
public class DashboardService {

	private final MetaOrmDao metaOrmDao;

	@Autowired
	public DashboardService(MetaOrmDao metaOrmDao) {
		this.metaOrmDao = metaOrmDao;
	}

	public CrmDashboard getDashboard(String dashboardId, boolean isCommonApi) {
		if (isCommonApi) {
			return metaOrmDao.getById(dashboardId, CrmDashboard.class,
					DaoQuery.keyValue(Constants.ACCOUNT_ID, Constants.RADYFY_ACCOUNT_ID));

		}
		return metaOrmDao.getById(dashboardId, CrmDashboard.class, null);
	}
}
