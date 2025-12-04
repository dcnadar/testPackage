package com.radyfy.common.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrmMediaCreateRequest {

  @NotEmpty(message = "Please provide name")
  private String name;

  @NotEmpty(message = "Please provide type")
  private String type;

  @Min(value = 10, message = "Size must be greater than 10 bytes")
  private Long size;

  @NotEmpty(message = "Please provide path")
  private String path;
}
