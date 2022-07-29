package com.ukonnra.whiterabbit.core.domain.journal;

import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.service.FullTextQueryService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

public interface JournalFullTextQueryService extends FullTextQueryService<JournalEntity> {
  Set<String> DEFAULT_FIELDS = Set.of("name", "description", "tags");

  @Service
  final class Implementation implements JournalFullTextQueryService {
    @Override
    public List<JournalEntity> handle(
        Collection<JournalEntity> entities, ExternalQuery.FullText query) {
      return entities.stream()
          .filter(
              user ->
                  Optional.ofNullable(query.fields()).orElse(DEFAULT_FIELDS).stream()
                      .anyMatch(
                          field ->
                              switch (field) {
                                case "name" -> user.getName().contains(query.value());
                                case "description" -> user.getDescription().contains(query.value());
                                case "tags" -> user.getTags().stream()
                                    .anyMatch(tag -> tag.contains(query.value()));
                                default -> false;
                              }))
          .toList();
    }
  }
}
