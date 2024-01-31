package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import org.springframework.hateoas.RepresentationModel;

public abstract class AbstractModel<E, M extends AbstractModel<E, M>>
    extends RepresentationModel<M> {
  protected AbstractModel(E dto) {}
}
