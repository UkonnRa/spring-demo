package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.group.GroupService;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.record.RecordService;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.data.util.Streamable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public abstract class TestSuite {
  protected static final Faker FAKER = new Faker();

  protected static final Set<String> ALL_SCOPES =
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
          RecordService.WRITE_SCOPE);

  protected final UserRepository userRepository;

  protected TestSuite(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void setAuthentication(final TaskInput.AuthUser input) {
    final var authorities =
        Optional.ofNullable(input.scopes()).orElse(ALL_SCOPES).stream()
            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
            .collect(Collectors.toSet());
    Streamable.of(
            this.userRepository.findAll(
                Optional.ofNullable(input.user()).orElse(QUserEntity.userEntity.id.isNotNull())))
        .stream()
        .findFirst()
        .<Authentication>map(
            user ->
                new UsernamePasswordAuthenticationToken(
                    input.authId().getTokenValue(), "", authorities))
        .ifPresent(auth -> SecurityContextHolder.getContext().setAuthentication(auth));
  }
}
