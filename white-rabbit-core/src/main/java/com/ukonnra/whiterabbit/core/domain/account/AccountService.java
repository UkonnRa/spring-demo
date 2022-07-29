package com.ukonnra.whiterabbit.core.domain.account;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.ukonnra.whiterabbit.core.CoreError;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.auth.AuthUser;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
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
public class AccountService
    extends WriteService<AccountEntity, AccountCommand, AccountQuery, AccountEntity.Dto> {
  public static final String READ_SCOPE = "white-rabbit_accounts:read";
  public static final String WRITE_SCOPE = "white-rabbit_accounts:write";

  private final JournalService journalService;
  private final AccountFullTextQueryService fullTextQueryService;

  protected AccountService(
      AccountRepository repository,
      JournalService journalService,
      AccountFullTextQueryService fullTextQueryService) {
    super(
        repository,
        Map.of(
            "name",
            new SortableField<>(QAccountEntity.accountEntity.name, AccountEntity::getName),
            "type",
            new SortableField<>(QAccountEntity.accountEntity.type, AccountEntity::getType),
            "strategy",
            new SortableField<>(QAccountEntity.accountEntity.strategy, AccountEntity::getStrategy),
            "unit",
            new SortableField<>(QAccountEntity.accountEntity.unit, AccountEntity::getUnit),
            "journal.name",
            new SortableField<>(
                QAccountEntity.accountEntity.journal.name,
                (entity) -> entity.getJournal().getName())));
    this.journalService = journalService;
    this.fullTextQueryService = fullTextQueryService;
  }

  @Override
  public String readScope() {
    return READ_SCOPE;
  }

  @Override
  protected String entityType() {
    return AccountEntity.TYPE;
  }

  @Override
  protected String defaultSort() {
    return "name";
  }

  @Override
  protected boolean doIsReadable(AuthUser authUser, AccountEntity entity) {
    return this.journalService.isReadable(authUser, entity.getJournal());
  }

  @Override
  protected void doCheckWriteable(AuthUser authUser, AccountEntity entity) {
    this.journalService.checkWriteable(authUser, entity.getJournal());
    if (entity.isArchived()) {
      throw new CoreError.AlreadyArchived(entityType(), entity.getId());
    }
  }

  @Override
  protected Map.Entry<BooleanExpression, List<ExternalQuery>> parseQuery(AccountQuery query) {
    final var builder = QAccountEntity.accountEntity;

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
    Optional.ofNullable(query.strategy())
        .ifPresent(item -> expressions.add(builder.strategy.eq(item)));
    Optional.ofNullable(query.unit()).ifPresent(item -> expressions.add(builder.unit.eq(item)));
    if (!Optional.ofNullable(query.includeArchived()).orElse(false)) {
      expressions.add(builder.archived.eq(false));
    }

    return Map.entry(
        Optional.ofNullable(Expressions.allOf(expressions.toArray(new BooleanExpression[] {})))
            .orElse(builder.id.isNotNull()),
        externalQueries);
  }

  @Override
  protected List<AccountEntity> handleExternalQuery(
      AuthUser authUser, List<AccountEntity> entities, ExternalQuery query) {
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
  protected Optional<AccountEntity> doHandle(
      AuthUser authUser, AccountCommand command, @Nullable AccountEntity entity) {
    if (command instanceof AccountCommand.Create create) {
      return Optional.of(this.create(create));
    } else if (command instanceof AccountCommand.Update update) {
      return Optional.of(this.update(update, entity));
    } else if (command instanceof AccountCommand.Delete delete) {
      this.delete(delete, entity);
    }
    return Optional.empty();
  }

  private AccountEntity create(final AccountCommand.Create command) {
    final var builder = QAccountEntity.accountEntity;
    if (this.repository
        .findOne(builder.journal.id.eq(command.journal()).and(builder.name.eq(command.name())))
        .isPresent()) {
      throw new CoreError.AlreadyExist(entityType(), "name", command.name());
    }

    final var journal =
        this.journalService
            .findOne(command.journal())
            .orElseThrow(
                () -> new CoreError.NotFound(JournalEntity.TYPE, command.journal().toString()));

    return this.repository.save(
        new AccountEntity(
            journal,
            command.name(),
            command.description(),
            command.accountType(),
            command.strategy(),
            command.unit(),
            false));
  }

  private AccountEntity update(
      final AccountCommand.Update command, final @Nullable AccountEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    if (Optional.ofNullable(command.name())
        .map(
            name -> this.repository.findOne(QAccountEntity.accountEntity.name.eq(name)).isPresent())
        .orElse(false)) {
      throw new CoreError.AlreadyExist(entityType(), "name", command.name());
    }

    if (command.name() == null
        && command.description() == null
        && command.accountType() == null
        && command.strategy() == null
        && command.unit() == null) {
      return entity;
    }

    Optional.ofNullable(command.name()).ifPresent(entity::setName);
    Optional.ofNullable(command.description()).ifPresent(entity::setDescription);
    Optional.ofNullable(command.accountType()).ifPresent(entity::setType);
    Optional.ofNullable(command.strategy()).ifPresent(entity::setStrategy);
    Optional.ofNullable(command.unit()).ifPresent(entity::setUnit);

    return this.repository.save(entity);
  }

  private void delete(final AccountCommand.Delete command, final @Nullable AccountEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    this.repository.delete(entity);
  }
}
