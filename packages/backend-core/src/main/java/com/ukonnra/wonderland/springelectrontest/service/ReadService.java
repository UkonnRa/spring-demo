package com.ukonnra.wonderland.springelectrontest.service;

import java.util.List;
import java.util.Optional;

public interface ReadService<E, Q> {
  List<E> findAll(final Q query);

  Optional<E> findOne(final Q query);
}