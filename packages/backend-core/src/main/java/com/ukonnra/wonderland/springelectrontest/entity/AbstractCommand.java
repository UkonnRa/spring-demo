package com.ukonnra.wonderland.springelectrontest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface AbstractCommand {
  @JsonProperty
  String type();
}
