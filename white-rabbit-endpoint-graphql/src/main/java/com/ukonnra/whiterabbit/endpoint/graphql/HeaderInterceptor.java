package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.auth.AuthUser;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.group.GroupService;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.record.RecordService;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import java.util.Collections;
import java.util.Set;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Component
class HeaderInterceptor implements WebGraphQlInterceptor {
  private final UserRepository userRepository;

  HeaderInterceptor(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
    final var user =
        this.userRepository
            .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
            .iterator()
            .next();
    final var authId = user.getAuthIds().stream().findFirst().orElseThrow();
    final var authUser =
        new AuthUser(
            user,
            authId,
            Set.of(
                UserService.READ_SCOPE,
                UserService.WRITE_SCOPE,
                GroupService.READ_SCOPE,
                GroupService.WRITE_SCOPE,
                JournalService.READ_SCOPE,
                JournalService.WRITE_SCOPE,
                AccountService.READ_SCOPE,
                AccountService.WRITE_SCOPE,
                RecordService.READ_SCOPE,
                RecordService.WRITE_SCOPE));

    request.configureExecutionInput(
        (executionInput, builder) ->
            builder.graphQLContext(Collections.singletonMap("authUser", authUser)).build());

    return chain.next(request);
  }
}
