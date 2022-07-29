package com.ukonnra.whiterabbit.core.domain.record;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.auth.AuthUser;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecordService
    extends WriteService<RecordEntity, RecordCommand, RecordQuery, RecordEntity.Dto> {
  public static final String READ_SCOPE = "white-rabbit_records:read";
  public static final String WRITE_SCOPE = "white-rabbit_records:write";

  private final AccountService accountService;
  private final RecordFullTextQueryService fullTextQueryService;

  protected RecordService(
      RecordRepository repository,
      AccountService accountService,
      RecordFullTextQueryService fullTextQueryService) {
    super(
        repository,
        Map.of(
            "name",
            new SortableField<>(QRecordEntity.recordEntity.name, RecordEntity::getName),
            "type",
            new SortableField<>(QRecordEntity.recordEntity.type, RecordEntity::getType),
            "date",
            new SortableField<>(QRecordEntity.recordEntity.date, RecordEntity::getDate),
            "journal.name",
            new SortableField<>(
                QRecordEntity.recordEntity.journal.name,
                (entity) -> entity.getJournal().getName())));
    this.accountService = accountService;
    this.fullTextQueryService = fullTextQueryService;
  }

  @Override
  public String readScope() {
    return READ_SCOPE;
  }

  @Override
  protected String entityType() {
    return RecordEntity.TYPE;
  }

  @Override
  protected String defaultSort() {
    return "name";
  }

  @Override
  protected boolean doIsReadable(AuthUser authUser, RecordEntity entity) {
    return entity.getItems().stream()
        .allMatch(item -> this.accountService.isReadable(authUser, item.getAccount()));
  }

  @Override
  protected void doCheckWriteable(AuthUser authUser, RecordEntity entity) {
    for (final var item : entity.getItems()) {
      this.accountService.checkWriteable(authUser, item.getAccount());
    }
  }

  @Override
  protected Map.Entry<BooleanExpression, List<ExternalQuery>> parseQuery(RecordQuery query) {
    final var builder = QRecordEntity.recordEntity;

    final var expressions = new ArrayList<BooleanExpression>();
    final var externalQueries = new ArrayList<ExternalQuery>();

    Optional.ofNullable(query.fullText()).ifPresent(externalQueries::add);
    Optional.ofNullable(query.id()).ifPresent(item -> expressions.add(builder.id.in(item.idSet())));
    Optional.ofNullable(query.journal())
        .ifPresent(item -> expressions.add(builder.journal.id.eq(item)));
    Optional.ofNullable(query.name())
        .ifPresent(
            item -> {
              if (item instanceof TextQuery.Eq eq) {
                expressions.add(builder.name.eq(eq.value()));
              } else if (item instanceof TextQuery.FullText fullText) {
                externalQueries.add(new ExternalQuery.FullText(fullText.value(), Set.of("name")));
              }
            });
    Optional.ofNullable(query.description())
        .ifPresent(
            item -> externalQueries.add(new ExternalQuery.FullText(item, Set.of("description"))));
    Optional.ofNullable(query.type()).ifPresent(item -> expressions.add(builder.type.eq(item)));
    Optional.ofNullable(query.date())
        .ifPresent(
            range -> {
              Optional.ofNullable(range.gt()).map(builder.date::gt).ifPresent(expressions::add);
              Optional.ofNullable(range.lt()).map(builder.date::lt).ifPresent(expressions::add);
            });
    Optional.ofNullable(query.tag())
        .ifPresent(
            item -> {
              if (item instanceof TextQuery.Eq eq) {
                expressions.add(builder.tags.contains(eq.value()));
              } else if (item instanceof TextQuery.FullText fullText) {
                externalQueries.add(new ExternalQuery.FullText(fullText.value(), Set.of("tags")));
              }
            });

    return Map.entry(
        Optional.ofNullable(Expressions.allOf(expressions.toArray(new BooleanExpression[] {})))
            .orElse(builder.id.isNotNull()),
        externalQueries);
  }

  @Override
  protected List<RecordEntity> handleExternalQuery(
      AuthUser authUser, List<RecordEntity> entities, ExternalQuery query) {
    if (query instanceof ExternalQuery.FullText fullText) {
      return this.fullTextQueryService.handle(entities, fullText);
    }
    return entities;
  }

  @Override
  public String writeScope() {
    return WRITE_SCOPE;
  }

  @Override
  protected Optional<RecordEntity> doHandle(
      AuthUser authUser, RecordCommand command, @Nullable RecordEntity entity) {
    return Optional.empty();
  }
}
