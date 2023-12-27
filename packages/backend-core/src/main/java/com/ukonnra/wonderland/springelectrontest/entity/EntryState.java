package com.ukonnra.wonderland.springelectrontest.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = EntryState.Valid.class, name = "VALID"),
  @JsonSubTypes.Type(value = EntryState.Invalid.class, name = "INVALID")
})
public sealed interface EntryState {
  record Valid(BigDecimal value) implements EntryState {}

  record Invalid(BigDecimal left, BigDecimal right) implements EntryState {}
}
