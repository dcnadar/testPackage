package com.radyfy.common.model.enums;

public enum  Gender {

	MALE("Male"), FEMALE("Female"), OTHERS("Others");

	private String name;
	Gender(String name){
		this.name = name;
	}

	public String value(){
		return this.name;
	}
}
