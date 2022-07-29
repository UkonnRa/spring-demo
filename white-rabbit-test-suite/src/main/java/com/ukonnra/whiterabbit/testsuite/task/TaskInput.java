package com.ukonnra.whiterabbit.testsuite.task;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ukonnra.whiterabbit.core.Command;
import com.ukonnra.whiterabbit.core.domain.user.AuthIdValue;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.core.query.Query;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

public sealed interface TaskInput permits TaskInput.Read, TaskInput.Write {
  @Builder
  record AuthUser(
      @Nullable BooleanExpression user,
      @Nullable AuthIdValue authId,
      @Nullable Set<String> scopes) {
    public AuthUser(final UserEntity user, @Nullable Set<String> scopes) {
      this(
          QUserEntity.userEntity.id.eq(user.getId()),
          user.getAuthIds().stream().findFirst().orElse(null),
          scopes);
    }
  }

  AuthUser authUser();

  sealed interface Read extends TaskInput {

    record FindOne<Q extends Query>(AuthUser authUser, Q query) implements Read {}

    record FindPage<Q extends Query>(AuthUser authUser, Pagination pagination, Sort sort, Q query)
        implements Read {}
  }

  sealed interface Write extends TaskInput permits Write.HandleCommand, Write.HandleCommands {
    record HandleCommand<C extends Command>(AuthUser authUser, C command) implements Write {}

    record HandleCommands<C extends Command>(AuthUser authUser, List<C> commands)
        implements Write {}
  }
}
