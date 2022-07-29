package com.ukonnra.whiterabbit.core;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractRepository<E>
    extends JpaRepository<E, UUID>, QuerydslPredicateExecutor<E> {}
