package com.ukonnra.whiterabbit.core.domain.group;

import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.service.FullTextQueryService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

public interface GroupFullTextQueryService extends FullTextQueryService<GroupEntity> {
  Set<String> DEFAULT_FIELDS = Set.of("name", "descriptions");

  @Service
  final class Implementation implements GroupFullTextQueryService {
    @Override
    public List<GroupEntity> handle(
        Collection<GroupEntity> entities, ExternalQuery.FullText query) {
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
