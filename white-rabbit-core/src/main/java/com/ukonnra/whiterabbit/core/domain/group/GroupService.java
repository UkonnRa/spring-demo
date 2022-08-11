package com.ukonnra.whiterabbit.core.domain.group;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.ukonnra.whiterabbit.core.CoreError;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserQuery;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GroupService
    extends WriteService<GroupEntity, GroupCommand, GroupQuery, GroupEntity.Dto> {
  public static final String READ_SCOPE = "white-rabbit_groups:read";
  public static final String WRITE_SCOPE = "white-rabbit_groups:write";

  private final GroupFullTextQueryService fullTextQueryService;

  private final UserService userService;

  protected GroupService(
      UserRepository userRepository,
      GroupRepository repository,
      GroupFullTextQueryService fullTextQueryService,
      UserService userService) {
    super(
        userRepository,
        repository,
        Map.of("name", new SortableField<>(QGroupEntity.groupEntity.name, GroupEntity::getName)));
    this.fullTextQueryService = fullTextQueryService;
    this.userService = userService;
  }

  @Override
  protected String entityType() {
    return GroupEntity.TYPE;
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
  public Map.Entry<BooleanExpression, List<ExternalQuery>> parseQuery(GroupQuery query) {
    final var builder = QGroupEntity.groupEntity;

    final var expressions = new ArrayList<BooleanExpression>();
    final var externalQueries = new ArrayList<ExternalQuery>();

    Optional.ofNullable(query.fullText()).ifPresent(externalQueries::add);
    Optional.ofNullable(query.containingUser())
        .ifPresent(
            item ->
                externalQueries.add(
                    new ExternalQuery.ContainingUser(item, Set.of("admins", "members"))));
    Optional.ofNullable(query.id()).ifPresent(item -> expressions.add(builder.id.in(item.idSet())));

    if (query.name() instanceof TextQuery.Eq eq) {
      expressions.add(builder.name.eq(eq.value()));
    } else if (query.name() instanceof TextQuery.FullText fullText) {
      externalQueries.add(new ExternalQuery.FullText(fullText.value(), Set.of("name")));
    }

    Optional.ofNullable(query.description())
        .ifPresent(
            item -> externalQueries.add(new ExternalQuery.FullText(item, Set.of("description"))));
    Optional.ofNullable(query.admins())
        .ifPresent(item -> expressions.add(builder.admins.any().id.in(item)));
    Optional.ofNullable(query.members())
        .ifPresent(item -> expressions.add(builder.members.any().id.in(item)));

    return Map.entry(
        Optional.ofNullable(Expressions.allOf(expressions.toArray(new BooleanExpression[] {})))
            .orElse(builder.id.isNotNull()),
        externalQueries);
  }

  @Override
  protected List<GroupEntity> handleExternalQuery(
      final @Nullable UserEntity user, List<GroupEntity> entities, ExternalQuery query) {
    if (query instanceof ExternalQuery.FullText fullText) {
      return this.fullTextQueryService.handle(entities, fullText);
    } else if (query instanceof ExternalQuery.ContainingUser containingUser) {
      return entities.stream()
          .filter(
              entity ->
                  Optional.ofNullable(containingUser.fields()).stream()
                      .flatMap(Collection::stream)
                      .anyMatch(
                          field -> {
                            if (field.equals("admins")) {
                              return entity.getAdmins().stream()
                                  .anyMatch(u -> u.getId().equals(containingUser.userId()));
                            } else if (field.equals("members")) {
                              return entity.getMembers().stream()
                                  .anyMatch(u -> u.getId().equals(containingUser.userId()));
                            }
                            return false;
                          }))
          .toList();
    }

    return entities;
  }

  @Override
  public Optional<GroupEntity> doHandle(
      final @Nullable UserEntity user, GroupCommand command, @Nullable GroupEntity entity) {
    var result = Optional.<GroupEntity>empty();
    if (command instanceof GroupCommand.Create create) {
      result = Optional.of(this.create(user, create));
    } else if (command instanceof GroupCommand.Update update) {
      result = Optional.of(this.update(update, entity));
    } else if (command instanceof GroupCommand.Delete delete) {
      this.delete(delete, entity);
    }
    return result;
  }

  @Override
  protected void doCheckWriteable(final @Nullable UserEntity user, GroupEntity entity) {
    super.doCheckWriteable(user, entity);

    if (!Optional.ofNullable(user).map(u -> entity.getAdmins().contains(u)).orElse(false)) {
      throw CoreError.NoPermission.write(entityType(), entity.getId().toString());
    }
  }

  private GroupEntity create(final @Nullable UserEntity user, final GroupCommand.Create command) {
    if (this.repository.findOne(QGroupEntity.groupEntity.name.eq(command.name())).isPresent()) {
      throw new CoreError.AlreadyExist(entityType(), "name", command.name());
    }

    final var adminIds = new HashSet<>(command.admins());
    Optional.ofNullable(user).ifPresent(u -> adminIds.add(u.getId()));

    final var admins =
        this.userService.findAll(Sort.unsorted(), command.admins().size(), new UserQuery(adminIds));
    final var members =
        this.userService.findAll(
            Sort.unsorted(), command.members().size(), new UserQuery(command.members()));

    return this.repository.save(
        new GroupEntity(
            command.name(), command.description(), new HashSet<>(admins), new HashSet<>(members)));
  }

  private GroupEntity update(
      final GroupCommand.Update command, final @Nullable GroupEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    Optional.ofNullable(command.name())
        .flatMap(name -> this.repository.findOne(QGroupEntity.groupEntity.name.eq(name)))
        .ifPresent(
            e -> {
              throw new CoreError.AlreadyExist(entityType(), "name", e.getName());
            });

    if (command.name() == null
        && command.description() == null
        && command.admins() == null
        && command.members() == null) {
      return entity;
    }

    Optional.ofNullable(command.name()).ifPresent(entity::setName);
    Optional.ofNullable(command.description()).ifPresent(entity::setDescription);
    Optional.ofNullable(command.admins())
        .map(ids -> this.userService.findAll(Sort.unsorted(), ids.size(), new UserQuery(ids)))
        .ifPresent(users -> entity.setAdmins(new HashSet<>(users)));
    Optional.ofNullable(command.members())
        .map(ids -> this.userService.findAll(Sort.unsorted(), ids.size(), new UserQuery(ids)))
        .ifPresent(users -> entity.setMembers(new HashSet<>(users)));

    return this.repository.save(entity);
  }

  private void delete(final GroupCommand.Delete command, final @Nullable GroupEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    this.repository.delete(entity);
  }
}
