package com.ukonnra.whiterabbit.core.domain.record;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.CoreError;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.domain.account.AccountRepository;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecordService
    extends WriteService<RecordEntity, RecordCommand, RecordQuery, RecordEntity.Dto> {
  public static final String READ_SCOPE = "white-rabbit_records:read";
  public static final String WRITE_SCOPE = "white-rabbit_records:write";

  private final JournalRepository journalRepository;
  private final AccountService accountService;
  private final AccountRepository accountRepository;
  private final RecordFullTextQueryService fullTextQueryService;

  protected RecordService(
      UserRepository userRepository,
      RecordRepository repository,
      JournalRepository journalRepository,
      AccountService accountService,
      AccountRepository accountRepository,
      RecordFullTextQueryService fullTextQueryService) {
    super(
        userRepository,
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
                QRecordEntity.recordEntity.journal.name, entity -> entity.getJournal().getName())));
    this.journalRepository = journalRepository;
    this.accountService = accountService;
    this.accountRepository = accountRepository;
    this.fullTextQueryService = fullTextQueryService;
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
  protected String writeScope() {
    return WRITE_SCOPE;
  }

  @Override
  protected String readScope() {
    return READ_SCOPE;
  }

  @Override
  protected boolean doIsReadable(final @Nullable UserEntity user, RecordEntity entity) {
    return entity.getItems().stream()
        .allMatch(item -> this.accountService.isReadable(user, item.getAccount()));
  }

  @Override
  protected void doCheckWriteable(@Nullable UserEntity user, RecordEntity entity) {
    super.doCheckWriteable(user, entity);

    for (final var item : entity.getItems()) {
      this.accountService.checkWriteable(user, item.getAccount());
    }
  }

  @Override
  public Map.Entry<BooleanExpression, List<ExternalQuery>> parseQuery(RecordQuery query) {
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
      final @Nullable UserEntity user, List<RecordEntity> entities, ExternalQuery query) {
    if (query instanceof ExternalQuery.FullText fullText) {
      return this.fullTextQueryService.handle(entities, fullText);
    }
    return entities;
  }

  @Override
  public Optional<RecordEntity> doHandle(
      final @Nullable UserEntity user, RecordCommand command, @Nullable RecordEntity entity) {
    if (command instanceof RecordCommand.Create create) {
      return Optional.of(this.create(user, create));
    } else if (command instanceof RecordCommand.Update update) {
      return Optional.of(this.update(user, update, entity));
    } else if (command instanceof RecordCommand.Delete delete) {
      this.delete(delete, entity);
    }
    return Optional.empty();
  }

  private Set<RecordItemValue> loadValidItems(
      final @Nullable UserEntity user,
      final JournalEntity journal,
      final Set<RecordItemValue.Dto> items) {
    final var accounts =
        this.accountRepository
            .findAllById(items.stream().map(RecordItemValue.Dto::account).toList())
            .stream()
            .collect(Collectors.toMap(AbstractEntity::getId, e -> e));
    for (final var account : accounts.values()) {
      if (!account.getJournal().equals(journal)) {
        throw new CoreError.AccountNotInJournal(journal, account);
      }
      this.accountService.checkWriteable(user, account);
    }
    return items.stream()
        .flatMap(
            item ->
                Optional.ofNullable(accounts.get(item.account()))
                    .map(account -> new RecordItemValue(account, item.amount(), item.price()))
                    .stream())
        .collect(Collectors.toSet());
  }

  private RecordEntity create(final @Nullable UserEntity user, final RecordCommand.Create command) {
    final var builder = QRecordEntity.recordEntity;
    if (this.repository
        .findOne(builder.journal.id.eq(command.journal()).and(builder.name.eq(command.name())))
        .isPresent()) {
      throw new CoreError.AlreadyExist(entityType(), "name", command.name());
    }

    final var journal =
        this.journalRepository
            .findById(command.journal())
            .orElseThrow(
                () -> new CoreError.NotFound(JournalEntity.TYPE, command.journal().toString()));
    final var items = this.loadValidItems(user, journal, command.items());

    final var record =
        new RecordEntity(
            journal,
            command.name(),
            command.description(),
            command.recordType(),
            command.date(),
            command.tags(),
            items);
    final var errors = record.validate();
    if (!errors.isEmpty()) {
      for (final var error : errors) {
        log.error("Error when validating record: {}", error.getMessage(), error);
      }
      throw new CoreError.Errors(errors);
    }
    return this.repository.save(record);
  }

  private RecordEntity update(
      final @Nullable UserEntity user,
      final RecordCommand.Update command,
      @Nullable final RecordEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    Optional.ofNullable(command.name())
        .map(
            name -> {
              final var builder = QRecordEntity.recordEntity;
              return builder.journal.eq(entity.getJournal()).and(builder.name.eq(name));
            })
        .flatMap(this.repository::findOne)
        .ifPresent(
            item -> {
              throw new CoreError.AlreadyExist(entityType(), "name", item.getName());
            });

    if (command.name() == null
        && command.description() == null
        && command.recordType() == null
        && command.date() == null
        && command.tags() == null
        && command.items() == null) {
      return entity;
    }

    Optional.ofNullable(command.name()).ifPresent(entity::setName);
    Optional.ofNullable(command.description()).ifPresent(entity::setDescription);
    Optional.ofNullable(command.recordType()).ifPresent(entity::setType);
    Optional.ofNullable(command.date()).ifPresent(entity::setDate);
    Optional.ofNullable(command.tags()).ifPresent(entity::setTags);
    Optional.ofNullable(command.items())
        .map(items -> this.loadValidItems(user, entity.getJournal(), items))
        .ifPresent(entity::setItems);

    return this.repository.save(entity);
  }

  private void delete(final RecordCommand.Delete command, final @Nullable RecordEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    this.repository.delete(entity);
  }
}
