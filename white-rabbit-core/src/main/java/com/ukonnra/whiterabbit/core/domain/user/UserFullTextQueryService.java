package com.ukonnra.whiterabbit.core.domain.user;

import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.service.FullTextQueryService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

public interface UserFullTextQueryService extends FullTextQueryService<UserEntity> {
  Set<String> DEFAULT_FIELDS = Set.of("name");

  @Service
  final class Implementation implements UserFullTextQueryService {
    @Override
    public List<UserEntity> handle(Collection<UserEntity> entities, ExternalQuery.FullText query) {
      return entities.stream()
          .filter(
              user ->
                  Optional.ofNullable(query.fields()).orElse(DEFAULT_FIELDS).stream()
                      .anyMatch(
                          field -> {
                            if (field.equals("name")) {
                              return user.getName().contains(query.value());
                            } else {
                              return false;
                            }
                          }))
          .toList();
    }
  }
}
