package com.radyfy.common.model.dynamic.table;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageParam implements Serializable{
	private String paramKey;
	private String paramValue;
	private boolean required;

}
