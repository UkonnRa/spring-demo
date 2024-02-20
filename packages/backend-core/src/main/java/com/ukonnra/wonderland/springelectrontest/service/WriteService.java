package com.ukonnra.wonderland.springelectrontest.service;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity;
import com.ukonnra.wonderland.springelectrontest.repository.Repository;
import jakarta.annotation.Nullable;
import jakarta.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

public interface WriteService<
        E extends AbstractEntity, Q extends Specification<E>, C, R extends Repository<E>, T>
    extends ReadService<E, Q> {
  List<E> handleCommand(final C command);

  List<T> convert(final Collection<E> entities);

  default Optional<T> convert(@Nullable final E entity) {
    return this.convert(Optional.ofNullable(entity));
  }

  default Optional<T> convert(final Optional<E> entity) {
    return this.convert(entity.stream().toList()).stream().findFirst();
  }

  R getRepository();

  Validator getValidator();

  @Override
  default List<E> findAll(final Q query) {
    return this.getRepository().findAll(query);
  }

  @Override
  default Optional<E> findOne(final Q query) {
    return this.getRepository().findOne(query);
  }
}
