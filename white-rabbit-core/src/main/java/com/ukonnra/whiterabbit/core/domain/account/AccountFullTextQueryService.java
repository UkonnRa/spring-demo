package com.ukonnra.whiterabbit.core.domain.account;

import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.service.FullTextQueryService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

public interface AccountFullTextQueryService extends FullTextQueryService<AccountEntity> {
  Set<String> DEFAULT_FIELDS = Set.of("name", "description", "tags");

  @Service
  final class Implementation implements AccountFullTextQueryService {
    @Override
    public List<AccountEntity> handle(
        Collection<AccountEntity> entities, ExternalQuery.FullText query) {
      return entities.stream()
          .filter(
              user ->
                  Optional.ofNullable(query.fields()).orElse(DEFAULT_FIELDS).stream()
                      .anyMatch(
                          field ->
                              switch (field) {
                                case "name" -> user.getName().contains(query.value());
                                case "description" -> user.getDescription().contains(query.value());
                                default -> false;
                              }))
          .toList();
    }
  }
}
