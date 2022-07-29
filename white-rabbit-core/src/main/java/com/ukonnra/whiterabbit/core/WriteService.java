package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.auth.AuthUser;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
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
    extends ReadService<E, Q> {
  protected WriteService(
      AbstractRepository<E> repository, Map<String, SortableField<E, ?>> sortableFields) {
    super(repository, sortableFields);
  }

  protected void doCheckWriteable(final AuthUser authUser, final E entity) {}

  public final void checkWriteable(final AuthUser authUser, final E entity) {
    if (!authUser.scopes().contains(readScope())) {
      throw CoreError.NoPermission.write(
          entityType(), entity == null ? null : entity.getId().toString());
    }

    // Admin can always writeable
    if (Optional.ofNullable(authUser.user())
        .map(u -> u.getRole().compareTo(RoleValue.USER) > 0)
        .orElse(false)) {
      return;
    }

    doCheckWriteable(authUser, entity);
  }

  public abstract String writeScope();

  @Override
  protected UUID getId(E entity) {
    return entity.getId();
  }

  protected abstract Optional<E> doHandle(
      final AuthUser authUser, final C command, @Nullable final E entity);

  @Transactional
  public Optional<E> handle(final AuthUser authUser, final C command) {
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
      this.checkWriteable(authUser, entity);
    }
    final var result = this.doHandle(authUser, command, entity);
    this.repository.flush();
    return result;
  }

  @Transactional
  public List<Optional<D>> handleAll(final AuthUser authUser, final List<C> commands) {
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
        this.checkWriteable(authUser, entity);
      }
      final var result =
          this.doHandle(
              authUser, command.withTargetId(realId.map(UUID::toString).orElse(null)), entity);
      results.add(result.map(e -> e.toDto()));
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
