package com.ukonnra.whiterabbit.endpoint.graphql.controller;

import com.ukonnra.whiterabbit.core.domain.user.AuthIdValue;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@Slf4j
@Transactional
public class UserController {
  private final UserRepository userRepository;

  public UserController(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @QueryMapping
  public Optional<UserEntity> user(@Argument("id") Optional<UUID> id) {
    if (id.isPresent()) {
      return this.userRepository.findById(id.get());
    }
    final var uuid = UUID.randomUUID();
    final var user =
        this.userRepository.save(
            UserEntity.builder()
                .name("Name " + uuid)
                .authIds(Set.of(new AuthIdValue("provider", "value " + uuid)))
                .build());
    return Optional.ofNullable(user.getId()).flatMap(this.userRepository::findById);
  }
}
