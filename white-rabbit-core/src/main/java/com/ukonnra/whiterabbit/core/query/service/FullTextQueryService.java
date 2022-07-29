package com.ukonnra.whiterabbit.core.query.service;

import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import java.util.Collection;
import java.util.List;

public interface FullTextQueryService<E> {
  List<E> handle(Collection<E> entities, ExternalQuery.FullText query);
}
