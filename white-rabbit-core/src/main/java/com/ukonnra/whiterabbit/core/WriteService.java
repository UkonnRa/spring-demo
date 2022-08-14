package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

public abstract class WriteService<
        E extends AbstractEntity<D>, C extends Command<C>, Q extends Query, D>
    extends ReadService<E, Q, D> {
  protected WriteService(
      UserRepository userRepository,
      AbstractRepository<E> repository,
      Map<String, SortableField<E, ?>> sortableFields) {
    super(userRepository, repository, sortableFields);
  }

  protected void doCheckWriteable(final @Nullable UserEntity user, final E entity) {}

  public abstract Optional<E> doHandle(
      final @Nullable UserEntity user, final C command, @Nullable final E entity);

  protected abstract String writeScope();

  @Override
  protected UUID getId(E entity) {
    return entity.getId();
  }

  @Override
  public D toDto(E entity) {
    return entity.toDto();
  }

  public final void checkWriteable(final @Nullable UserEntity user, final E entity) {
    if (this.getAuthentication().stream()
        .flatMap(a -> a.getAuthorities().stream())
        .noneMatch(authority -> authority.getAuthority().equals("SCOPE_" + this.writeScope()))) {
      throw CoreError.NoPermission.write(entityType(), this.getId(entity).toString());
    }

    // Admin can always writeable
    if (Optional.ofNullable(user)
        .map(u -> u.getRole().compareTo(RoleValue.USER) > 0)
        .orElse(false)) {
      return;
    }

    doCheckWriteable(user, entity);
  }

  public final void checkWriteable(final E entity) {
    final var user = this.getAuthUser();
    this.checkWriteable(user, entity);
  }

  @Transactional
  public Optional<E> handle(final C command) {
    final var user = this.getAuthUser();

    final var entity =
        Optional.ofNullable(command.targetId())
            .flatMap(
                id -> {
                  try {
                    return Optional.of(UUID.fromString(id));
                  } catch (IllegalArgumentException e) {
                    return Optional.empty();
                  }
                })
            .flatMap(this.repository::findById)
            .orElse(null);
    if (entity != null) {
      this.checkWriteable(user, entity);
    }
    final var result = this.doHandle(user, command, entity);
    this.repository.flush();
    return result;
  }

  @Transactional
  public List<Optional<D>> handleAll(final List<C> commands) {
    final var user = this.getAuthUser();

    final var idMap = new HashMap<String, UUID>();
    final var results = new ArrayList<Optional<D>>();

    for (final var command : commands) {
      final var realId =
          Optional.ofNullable(idMap.get(command.targetId()))
              .or(
                  () ->
                      Optional.ofNullable(command.targetId())
                          .flatMap(
                              id -> {
                                try {
                                  return Optional.of(UUID.fromString(id));
                                } catch (IllegalArgumentException e) {
                                  return Optional.empty();
                                }
                              }));
      final var entity = realId.flatMap(this.repository::findById).orElse(null);
      if (entity != null) {
        this.checkWriteable(user, entity);
      }
      final var result =
          this.doHandle(
              user, command.withTargetId(realId.map(UUID::toString).orElse(null)), entity);
      results.add(result.map(AbstractEntity::toDto));
      result
          .map(E::getId)
          .ifPresent(
              id ->
                  idMap.put(
                      Optional.ofNullable(command.targetId())
                          .orElseGet(() -> UUID.randomUUID().toString()),
                      id));
    }
    this.repository.flush();
    return results;
  }
}
