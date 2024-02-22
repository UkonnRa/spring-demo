package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AccountsModel extends RepresentationModel<AccountsModel> {
  private final List<AccountModel> values;

  public AccountsModel(List<AccountModel> values) {
    this.values = values;
  }
}
