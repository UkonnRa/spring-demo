package com.ukonnra.wonderland.springelectrontest.service;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ReadService<E, Q> {
  List<E> findAll(final Q query);

  default Optional<E> findOne(final Q query) {
    return this.findAll(query).stream().findFirst();
  }
}
