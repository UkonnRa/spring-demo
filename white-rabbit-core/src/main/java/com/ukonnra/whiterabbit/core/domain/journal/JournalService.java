package com.ukonnra.whiterabbit.core.domain.journal;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.ukonnra.whiterabbit.core.CoreError;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.auth.AuthUser;
import com.ukonnra.whiterabbit.core.domain.group.GroupService;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JournalService
    extends WriteService<JournalEntity, JournalCommand, JournalQuery, JournalEntity.Dto> {
  public static final String READ_SCOPE = "white-rabbit_journals:read";
  public static final String WRITE_SCOPE = "white-rabbit_journals:write";

  private final JournalFullTextQueryService fullTextQueryService;

  private final GroupService groupService;

  protected JournalService(
      JournalRepository repository,
      JournalFullTextQueryService fullTextQueryService,
      GroupService groupService) {
    super(
        repository,
        Map.of(
            "name", new SortableField<>(QJournalEntity.journalEntity.name, JournalEntity::getName),
            "unit",
                new SortableField<>(QJournalEntity.journalEntity.unit, JournalEntity::getUnit)));
    this.fullTextQueryService = fullTextQueryService;
    this.groupService = groupService;
  }

  @Override
  public String readScope() {
    return READ_SCOPE;
  }

  @Override
  protected String entityType() {
    return JournalEntity.TYPE;
  }

  @Override
  protected String defaultSort() {
    return "name";
  }

  @Override
  protected Map.Entry<BooleanExpression, List<ExternalQuery>> parseQuery(JournalQuery query) {
    final var builder = QJournalEntity.journalEntity;

    final var expressions = new ArrayList<BooleanExpression>();
    final var externalQueries = new ArrayList<ExternalQuery>();

    Optional.ofNullable(query.fullText()).ifPresent(externalQueries::add);
    Optional.ofNullable(query.containingUser())
        .ifPresent(
            item ->
                externalQueries.add(
                    new ExternalQuery.ContainingUser(item, Set.of("admins", "members", "tags"))));
    Optional.ofNullable(query.id()).ifPresent(item -> expressions.add(builder.id.in(item.idSet())));

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
    Optional.ofNullable(query.tags())
        .ifPresent(
            item -> {
              if (item instanceof TextQuery.Eq eq) {
                expressions.add(builder.tags.contains(eq.value()));
              } else if (item instanceof TextQuery.FullText fullText) {
                externalQueries.add(new ExternalQuery.FullText(fullText.value(), Set.of("tags")));
              }
            });
    Optional.ofNullable(query.unit()).ifPresent(item -> expressions.add(builder.unit.eq(item)));
    if (!Optional.ofNullable(query.includeArchived()).orElse(false)) {
      expressions.add(builder.archived.eq(false));
    }

    Optional.ofNullable(query.admin()).map(builder.admins::contains).ifPresent(expressions::add);
    Optional.ofNullable(query.member()).map(builder.members::contains).ifPresent(expressions::add);

    return Map.entry(
        Optional.ofNullable(Expressions.allOf(expressions.toArray(new BooleanExpression[] {})))
            .orElse(builder.id.isNotNull()),
        externalQueries);
  }

  public boolean isContainingUser(final Collection<AccessItemValue> items, final UUID userId) {
    if (items.stream()
        .anyMatch(
            item ->
                item.getItemType() == AccessItemValue.Type.USER && item.getId().equals(userId))) {
      return true;
    }

    return this.groupService
        .findAll(
            items.stream()
                .filter(item -> item.getItemType() == AccessItemValue.Type.GROUP)
                .map(AccessItemValue::getId)
                .toList())
        .values()
        .stream()
        .anyMatch(group -> group.isContainingUser(userId));
  }

  public boolean isContainingUser(final AccessItemValue item, final UUID userId) {
    if (item.getItemType() == AccessItemValue.Type.USER) {
      return item.getId().equals(userId);
    } else {
      return this.groupService
          .findOne(item.getId())
          .map(group -> group.isContainingUser(userId))
          .orElse(false);
    }
  }

  @Override
  protected List<JournalEntity> handleExternalQuery(
      AuthUser authUser, List<JournalEntity> entities, ExternalQuery query) {
    if (query instanceof ExternalQuery.FullText fullText) {
      return this.fullTextQueryService.handle(entities, fullText);
    } else if (query instanceof ExternalQuery.ContainingUser containingUser) {
      return entities.stream()
          .filter(
              entity ->
                  Optional.ofNullable(containingUser.fields()).stream()
                      .flatMap(Collection::stream)
                      .anyMatch(
                          field ->
                              switch (field) {
                                case "admins" -> this.isContainingUser(
                                    entity.getAdmins(), containingUser.userId());
                                case "members" -> this.isContainingUser(
                                    entity.getMembers(), containingUser.userId());
                                default -> false;
                              }))
          .toList();
    }

    return entities;
  }

  @Override
  protected boolean doIsReadable(AuthUser authUser, JournalEntity entity) {
    return Optional.ofNullable(authUser.user())
        .map(
            user ->
                this.isContainingUser(
                    Stream.concat(entity.getAdmins().stream(), entity.getMembers().stream())
                        .toList(),
                    user.getId()))
        .orElse(false);
  }

  @Override
  protected void doCheckWriteable(AuthUser authUser, JournalEntity entity) {
    if (Optional.ofNullable(authUser.user())
        .map(user -> !this.isContainingUser(entity.getAdmins(), user.getId()))
        .orElse(true)) {
      throw CoreError.NoPermission.write(entityType(), entity.getId().toString());
    }

    if (entity.isArchived()) {
      throw new CoreError.AlreadyArchived(entityType(), entity.getId());
    }
  }

  @Override
  public String writeScope() {
    return WRITE_SCOPE;
  }

  @Override
  protected Optional<JournalEntity> doHandle(
      AuthUser authUser, JournalCommand command, @Nullable JournalEntity entity) {
    if (command instanceof JournalCommand.Create create) {
      return Optional.of(this.create(create));
    } else if (command instanceof JournalCommand.Update update) {
      return Optional.of(this.update(update, entity));
    } else if (command instanceof JournalCommand.Delete delete) {
      this.delete(delete, entity);
    }
    return Optional.empty();
  }

  private JournalEntity create(final JournalCommand.Create command) {
    if (this.repository.findOne(QJournalEntity.journalEntity.name.eq(command.name())).isPresent()) {
      throw new CoreError.AlreadyExist(entityType(), "name", command.name());
    }

    final var journal = new JournalEntity();
    journal.setName(command.name());
    journal.setDescription(command.description());
    journal.setTags(command.tags());
    journal.setUnit(command.unit());
    journal.setAdmins(command.admins());
    journal.setMembers(command.members());

    return this.repository.save(journal);
  }

  private JournalEntity update(
      final JournalCommand.Update command, final @Nullable JournalEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    if (Optional.ofNullable(command.name())
        .map(
            name -> this.repository.findOne(QJournalEntity.journalEntity.name.eq(name)).isPresent())
        .orElse(false)) {
      throw new CoreError.AlreadyExist(entityType(), "name", command.name());
    }

    if (command.name() == null
        && command.description() == null
        && command.tags() == null
        && command.unit() == null
        && command.archived() == null
        && command.admins() == null
        && command.members() == null) {
      return entity;
    }

    Optional.ofNullable(command.name()).ifPresent(entity::setName);
    Optional.ofNullable(command.description()).ifPresent(entity::setDescription);
    Optional.ofNullable(command.tags()).ifPresent(entity::setTags);
    Optional.ofNullable(command.unit()).ifPresent(entity::setUnit);
    Optional.ofNullable(command.archived()).ifPresent(entity::setArchived);
    Optional.ofNullable(command.admins()).ifPresent(entity::setAdmins);
    Optional.ofNullable(command.members()).ifPresent(entity::setMembers);

    return this.repository.save(entity);
  }

  private void delete(final JournalCommand.Delete command, final @Nullable JournalEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    this.repository.delete(entity);
  }
}
