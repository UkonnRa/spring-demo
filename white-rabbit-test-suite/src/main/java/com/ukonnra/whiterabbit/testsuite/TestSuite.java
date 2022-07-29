package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.auth.AuthUser;
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
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.data.util.Streamable;

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

  protected AuthUser getAuthUser(final TaskInput.AuthUser input) {
    final var user =
        Streamable.of(
                this.userRepository.findAll(
                    Optional.ofNullable(input.user())
                        .orElse(QUserEntity.userEntity.id.isNotNull())))
            .stream()
            .findFirst();
    final var authId =
        user.flatMap(u -> u.getAuthIds().stream().findFirst())
            .or(() -> Optional.ofNullable(input.authId()))
            .orElseThrow();
    return new AuthUser(
        user.orElse(null), authId, Optional.ofNullable(input.scopes()).orElse(ALL_SCOPES));
  }
}
