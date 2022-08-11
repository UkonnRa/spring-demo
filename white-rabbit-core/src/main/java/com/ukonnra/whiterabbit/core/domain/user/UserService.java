package com.ukonnra.whiterabbit.core.domain.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.ukonnra.whiterabbit.core.CoreError;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService extends WriteService<UserEntity, UserCommand, UserQuery, UserEntity.Dto> {
  public static final String READ_SCOPE = "white-rabbit_users:read";
  public static final String WRITE_SCOPE = "white-rabbit_users:write";

  private final UserFullTextQueryService fullTextQueryService;

  protected UserService(UserRepository repository, UserFullTextQueryService fullTextQueryService) {
    super(
        repository,
        repository,
        Map.of("name", new SortableField<>(QUserEntity.userEntity.name, UserEntity::getName)));
    this.fullTextQueryService = fullTextQueryService;
  }

  @Override
  protected String entityType() {
    return UserEntity.TYPE;
  }

  @Override
  protected String defaultSort() {
    return "name";
  }

  @Override
  protected String readScope() {
    return READ_SCOPE;
  }

  @Override
  protected String writeScope() {
    return WRITE_SCOPE;
  }

  @Override
  public Map.Entry<BooleanExpression, List<ExternalQuery>> parseQuery(UserQuery query) {
    final var builder = QUserEntity.userEntity;

    final var expressions = new ArrayList<BooleanExpression>();
    final var externalQueries = new ArrayList<ExternalQuery>();

    Optional.ofNullable(query.id())
        .map(value -> builder.id.in(value.idSet()))
        .ifPresent(expressions::add);
    Optional.ofNullable(query.name())
        .ifPresent(
            value -> {
              if (value instanceof TextQuery.Eq eq) {
                expressions.add(builder.name.eq(eq.value()));
              } else if (value instanceof TextQuery.FullText fullText) {
                externalQueries.add(new ExternalQuery.FullText(fullText.value(), Set.of("name")));
              }
            });
    Optional.ofNullable(query.role()).map(builder.role::eq).ifPresent(expressions::add);
    Optional.ofNullable(query.authIdProviders())
        .map(value -> QUserEntity.userEntity.authIds.any().provider.in(value))
        .ifPresent(expressions::add);
    return Map.entry(
        Optional.ofNullable(Expressions.allOf(expressions.toArray(new BooleanExpression[] {})))
            .orElse(builder.id.isNotNull()),
        externalQueries);
  }

  @Override
  protected List<UserEntity> handleExternalQuery(
      final @Nullable UserEntity user, List<UserEntity> entities, ExternalQuery query) {
    if (query instanceof ExternalQuery.FullText fullText) {
      return this.fullTextQueryService.handle(entities, fullText);
    }
    return entities;
  }

  @Override
  public Optional<UserEntity> doHandle(
      @Nullable UserEntity user, UserCommand command, @Nullable UserEntity entity) {
    var result = Optional.<UserEntity>empty();
    if (command instanceof UserCommand.Create create) {
      result = Optional.of(this.create(user, create));
    } else if (command instanceof UserCommand.Update update) {
      result = Optional.of(this.update(user, update, entity));
    } else if (command instanceof UserCommand.Delete delete) {
      this.delete(user, delete, entity);
    }
    return result;
  }

  private static boolean isInvalidRole(@Nullable UserEntity user, @Nullable RoleValue role) {
    return Optional.ofNullable(user)
        .map(u -> role == null || u.getRole().compareTo(role) <= 0)
        .orElse(role != RoleValue.USER);
  }

  private UserEntity create(@Nullable UserEntity user, UserCommand.Create command) {
    if (command.role() != null && isInvalidRole(user, command.role())) {
      throw CoreError.NoPermission.write(entityType(), command.targetId());
    }

    if (this.repository.findOne(QUserEntity.userEntity.name.eq(command.name())).isPresent()) {
      throw new CoreError.AlreadyExist(entityType(), "name", command.name());
    }

    final var entity = new UserEntity();
    entity.setName(command.name());
    Optional.ofNullable(command.role()).ifPresent(entity::setRole);
    Optional.ofNullable(command.authIds()).ifPresent(entity::setAuthIds);
    return this.repository.save(entity);
  }

  private UserEntity update(
      @Nullable UserEntity user, UserCommand.Update command, @Nullable UserEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    if (isInvalidRole(user, entity.getRole())) {
      throw CoreError.NoPermission.write(entityType(), entity.getId().toString());
    }

    if (Optional.ofNullable(command.role()).map(role -> isInvalidRole(user, role)).orElse(false)) {
      throw CoreError.NoPermission.write(entityType(), command.targetId());
    }

    Optional.ofNullable(command.name())
        .flatMap(name -> this.repository.findOne(QUserEntity.userEntity.name.eq(name)))
        .ifPresent(
            e -> {
              throw new CoreError.AlreadyExist(entityType(), "name", e.getName());
            });

    if (command.name() == null && command.role() == null && command.authIds() == null) {
      return entity;
    }

    Optional.ofNullable(command.name()).ifPresent(entity::setName);
    Optional.ofNullable(command.role()).ifPresent(entity::setRole);
    Optional.ofNullable(command.authIds())
        .ifPresent(
            authIds -> {
              if (isInvalidRole(user, RoleValue.USER)) {
                throw CoreError.NoPermission.write(entityType(), command.targetId());
              }
              entity.setAuthIds(new HashSet<>(authIds));
            });

    return this.repository.save(entity);
  }

  private void delete(
      @Nullable UserEntity user, UserCommand.Delete command, @Nullable UserEntity entity) {
    if (entity == null) {
      throw new CoreError.NotFound(entityType(), command.targetId());
    }

    if (isInvalidRole(user, entity.getRole())) {
      throw CoreError.NoPermission.write(entityType(), entity.getId().toString());
    }

    this.repository.delete(entity);
  }
}
