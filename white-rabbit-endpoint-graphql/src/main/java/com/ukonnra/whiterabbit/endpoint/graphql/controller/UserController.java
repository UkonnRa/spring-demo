package com.ukonnra.whiterabbit.endpoint.graphql.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.auth.AuthUser;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserQuery;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.endpoint.graphql.model.FindPageInput;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@Slf4j
@Transactional
public class UserController {
  private final UserService userService;
  private final ObjectMapper objectMapper;

  public UserController(UserService userService, ObjectMapper objectMapper) {
    this.userService = userService;
    this.objectMapper = objectMapper;
  }

  @QueryMapping
  public Optional<UserEntity> user(
      @ContextValue AuthUser authUser, @Argument("query") String rawQuery)
      throws JsonProcessingException {
    final var query = this.objectMapper.readValue(rawQuery, UserQuery.class);
    return this.userService.findOne(authUser, query);
  }

  @QueryMapping
  public GraphQlPage<UserEntity> users(
      @ContextValue AuthUser authUser, @Arguments FindPageInput input)
      throws JsonProcessingException {
    final var query = this.objectMapper.readValue(input.query(), UserQuery.class);
    final var page =
        this.userService.findPage(authUser, input.pagination(), input.parsedSort(), query);
    return new GraphQlPage<>(page);
  }
}
