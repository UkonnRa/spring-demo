package com.ukonnra.whiterabbit.core;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.Page;
import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.core.query.Query;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class ReadService<E, Q extends Query, D> {
  protected final UserRepository userRepository;
  protected final AbstractRepository<E> repository;
  private final Map<String, SortableField<E, ?>> sortableFields;

  protected ReadService(
      UserRepository userRepository,
      AbstractRepository<E> repository,
      Map<String, SortableField<E, ?>> sortableFields) {
    this.userRepository = userRepository;
    this.repository = repository;
    this.sortableFields = sortableFields;
  }

  protected abstract String entityType();

  protected abstract String defaultSort();

  protected abstract String readScope();

  protected abstract UUID getId(final E entity);

  public abstract D toDto(final E entity);

  protected boolean doIsReadable(final @Nullable UserEntity user, final E entity) {
    return true;
  }

  private void checkReadable() {
    if (this.getAuthentication().stream()
        .flatMap(a -> a.getAuthorities().stream())
        .noneMatch(authority -> authority.getAuthority().equals("SCOPE_" + this.readScope()))) {
      throw CoreError.NoPermission.read(this.entityType(), null);
    }
  }

  public final boolean isReadable(final @Nullable UserEntity user, final E entity) {

    // Admin can always writeable
    if (Optional.ofNullable(user)
        .map(u -> u.getRole().compareTo(RoleValue.USER) > 0)
        .orElse(false)) {
      return true;
    }

    return doIsReadable(user, entity);
  }

  protected abstract Map.Entry<BooleanExpression, List<ExternalQuery>> parseQuery(Q query);

  protected abstract List<E> handleExternalQuery(
      final @Nullable UserEntity user, final List<E> entities, final ExternalQuery query);

  public Optional<E> findByCursor(final String cursor) {
    return this.repository.findById(Utils.decodeCursor(cursor));
  }

  @Transactional
  public Optional<E> findOne(final Q query) {
    final var user = this.getAuthUser();
    this.checkReadable();
    return this.doFindAll(user, this.parseQuery(query), Sort.unsorted(), Pagination.DEFAULT)
        .stream()
        .findFirst();
  }

  @Transactional
  public Optional<E> findOne(final UUID id) {
    final var user = this.getAuthUser();
    this.checkReadable();
    return this.repository.findById(id).filter(result -> this.isReadable(user, result));
  }

  @Transactional
  public Page<E> findPage(final Pagination pagination, Sort sort, final Q query) {
    final var user = this.getAuthUser();
    this.checkReadable();

    final var filteredSort = this.filterSort(sort);

    final var before =
        Optional.ofNullable(pagination.before()).flatMap(this::findByCursor).orElse(null);
    final var after =
        Optional.ofNullable(pagination.after()).flatMap(this::findByCursor).orElse(null);

    final var reversed = before != null && after == null;
    final var normalizedSort = normalizeSort(filteredSort, reversed);
    final var idSort = Sort.by(reversed ? Sort.Order.desc("id") : Sort.Order.asc("id"));

    final var queryPair = this.parseQuery(query);
    final var cursorQuery = this.parseCursorQuery(before, after, filteredSort);

    final var entities =
        this.doFindAll(
            user,
            Map.entry(queryPair.getKey().and(cursorQuery), queryPair.getValue()),
            normalizedSort.and(idSort),
            pagination);

    final var exceeded = entities.size() > pagination.size();

    final var hasNext = (!reversed && exceeded) || (reversed && before != null);
    final var hasPrevious = (reversed && exceeded) || (!reversed && after != null);

    var pageItems =
        new ArrayList<>(
            entities.subList(0, exceeded ? pagination.size() : entities.size()).stream()
                .map(data -> new Page.Item<>(Utils.encodeCursor(this.getId(data)), data))
                .toList());

    if (reversed) {
      Collections.reverse(pageItems);
    }

    return new Page<>(
        new Page.Info(
            hasPrevious,
            hasNext,
            pageItems.isEmpty() ? null : pageItems.get(0).cursor(),
            pageItems.isEmpty() ? null : pageItems.get(pageItems.size() - 1).cursor()),
        pageItems);
  }

  @Transactional
  public List<E> findAll(final Sort sort, int size, final Q query) {
    final var user = this.getAuthUser();
    this.checkReadable();

    return this.doFindAll(
        user,
        this.parseQuery(query),
        normalizeSort(sort, false),
        new Pagination(null, null, size, 0));
  }

  private List<E> doFindAll(
      final @Nullable UserEntity user,
      final Map.Entry<BooleanExpression, List<ExternalQuery>> query,
      final Sort sort,
      final Pagination pagination) {
    int cnt = 0;
    final var result = new ArrayList<E>();

    while (true) {
      var searched =
          this.repository
              .findAll(query.getKey(), PageRequest.of(cnt, pagination.size() + 1, sort))
              .getContent()
              .stream()
              .filter(e -> this.isReadable(user, e))
              .toList();
      cnt += 1;
      if (searched.isEmpty()) {
        break;
      }

      for (final var externalQuery : query.getValue()) {
        searched = this.handleExternalQuery(user, searched, externalQuery);
        if (searched.isEmpty()) {
          break;
        }
      }

      result.addAll(searched);
      if (result.size() >= pagination.size() + 1) {
        break;
      }
    }

    return result;
  }

  private static Sort normalizeSort(final Sort sort, boolean reversed) {
    final var orders =
        sort.stream()
            .map(
                order ->
                    (order.isAscending() && reversed) || (!order.isAscending() && !reversed)
                        ? Sort.Order.desc(order.getProperty())
                        : Sort.Order.asc(order.getProperty()))
            .toList();
    return Sort.by(orders);
  }

  protected Optional<Authentication> getAuthentication() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
  }

  protected @Nullable UserEntity getAuthUser() {
    return this.getAuthentication().map(Principal::getName).stream()
        .flatMap(
            name ->
                Streamable.of(
                    this.userRepository.findAll(
                        QUserEntity.userEntity.authIds.any().tokenValue.eq(name)))
                    .stream())
        .findFirst()
        .orElse(null);
  }

  private Sort filterSort(final Sort sort) {
    final var filtered =
        Sort.by(
            sort.filter(order -> this.sortableFields.containsKey(order.getProperty())).toList());
    if (filtered.isEmpty()) {
      return Sort.by(Sort.Order.asc(this.defaultSort()));
    }
    return filtered;
  }

  private @Nullable BooleanExpression parseCursorQuery(
      final @Nullable E before, final @Nullable E after, final Sort sort) {
    final BooleanExpression[] expression =
        sort.stream()
            .map(
                order -> {
                  final var field = this.sortableFields.get(order.getProperty());
                  if (field == null) {
                    return null;
                  }
                  final var values = new ArrayList<BooleanExpression>();
                  Optional.ofNullable(before)
                      .map(entity -> field.expression(entity, order.isAscending(), false))
                      .ifPresent(values::add);
                  Optional.ofNullable(after)
                      .map(entity -> field.expression(entity, order.isAscending(), true))
                      .ifPresent(values::add);
                  return Expressions.allOf(values.toArray(BooleanExpression[]::new));
                })
            .filter(Objects::nonNull)
            .toArray(BooleanExpression[]::new);
    return Expressions.allOf(expression);
  }

  public record SortableField<E, T extends Comparable<?>>(
      ComparableExpression<T> path, Function<E, T> getter) {

    public BooleanExpression expression(final E entity, boolean isAscending, boolean isAfter) {
      final var value = getter.apply(entity);
      return ((isAscending && isAfter) || (!isAscending && !isAfter))
          ? path.gt(value)
          : path.lt(value);
    }
  }
}
