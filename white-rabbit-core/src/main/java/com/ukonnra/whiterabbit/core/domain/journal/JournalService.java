package com.ukonnra.whiterabbit.core.domain.journal;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.ukonnra.whiterabbit.core.CoreError;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.domain.group.GroupRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
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

  private final GroupRepository groupRepository;

  protected JournalService(
      UserRepository userRepository,
      JournalRepository repository,
      JournalFullTextQueryService fullTextQueryService,
      GroupRepository groupRepository) {
    super(
        userRepository,
        repository,
        Map.of(
            "name", new SortableField<>(QJournalEntity.journalEntity.name, JournalEntity::getName),
            "unit",
                new SortableField<>(QJournalEntity.journalEntity.unit, JournalEntity::getUnit)));
    this.fullTextQueryService = fullTextQueryService;
    this.groupRepository = groupRepository;
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
  protected String writeScope() {
    return WRITE_SCOPE;
  }

  @Override
  protected String readScope() {
    return READ_SCOPE;
  }

  @Override
  protected UUID getId(JournalEntity entity) {
    return entity.getId();
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

    return this.groupRepository
        .findAllById(
            items.stream()
                .filter(item -> item.getItemType() == AccessItemValue.Type.GROUP)
                .map(AccessItemValue::getId)
                .toList())
        .stream()
        .anyMatch(group -> group.isContainingUser(userId));
  }

  @Override
  protected List<JournalEntity> handleExternalQuery(
      final @Nullable UserEntity user, List<JournalEntity> entities, ExternalQuery query) {
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
  protected boolean doIsReadable(final @Nullable UserEntity user, JournalEntity entity) {
    return Optional.ofNullable(user)
        .map(
            u ->
                this.isContainingUser(
                    Stream.concat(entity.getAdmins().stream(), entity.getMembers().stream())
                        .toList(),
                    u.getId()))
        .orElse(false);
  }

  @Override
  protected void doCheckWriteable(final @Nullable UserEntity user, JournalEntity entity) {
    super.doCheckWriteable(user, entity);

    if (Optional.ofNullable(user)
        .map(u -> !this.isContainingUser(entity.getAdmins(), u.getId()))
        .orElse(true)) {
      throw CoreError.NoPermission.write(entityType(), entity.getId().toString());
    }

    if (entity.isArchived()) {
      throw new CoreError.AlreadyArchived(entityType(), entity.getId());
    }
  }

  @Override
  public Optional<JournalEntity> doHandle(
      final @Nullable UserEntity user, JournalCommand command, @Nullable JournalEntity entity) {
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

    Optional.ofNullable(command.name())
        .flatMap(name -> this.repository.findOne(QJournalEntity.journalEntity.name.eq(name)))
        .ifPresent(
            e -> {
              throw new CoreError.AlreadyExist(entityType(), "name", e.getName());
            });

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
