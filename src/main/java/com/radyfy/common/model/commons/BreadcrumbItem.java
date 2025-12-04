package com.radyfy.common.model.commons;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BreadcrumbItem implements Serializable{

  private String name;
  private String slug;

  public BreadcrumbItem(){
    super();
  }

  public BreadcrumbItem(String name, String slug){
    super();
    this.name = name;
    this.slug = slug;
  }

}
