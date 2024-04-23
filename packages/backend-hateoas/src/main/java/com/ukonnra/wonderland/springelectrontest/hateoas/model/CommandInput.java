package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractCommand;

public interface CommandInput<C extends AbstractCommand> {
  String getType();

  C generateCommand();
}
