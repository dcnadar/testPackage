package com.radyfy.common.model.crm.page;

import com.radyfy.common.model.crm.grid.GridRequestParams;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityActionMeta {
	// crm_model id
	private String entityId;
	private GridRequestParams params;

	public EntityActionMeta(String entityId, GridRequestParams params) {
		this.entityId = entityId;
		if (params == null) {
			this.params = new GridRequestParams();
		} else {
			this.params = params;
		}
	}

}
