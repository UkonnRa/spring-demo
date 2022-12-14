package com.ukonnra.whiterabbit.endpoint.graphql.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.user.UserCommand;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserQuery;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.endpoint.graphql.model.FindPageInput;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlOrder;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@Slf4j
@Transactional
@SchemaMapping(typeName = UserController.TYPE)
public class UserController {
  public static final String TYPE = "User";
  private final UserService userService;
  private final ObjectMapper objectMapper;

  public UserController(UserService userService, ObjectMapper objectMapper) {
    this.userService = userService;
    this.objectMapper = objectMapper;
  }

  @QueryMapping
  public Optional<UserEntity.Dto> user(@Argument("query") final String rawQuery)
      throws JsonProcessingException {
    final var query = this.objectMapper.readValue(rawQuery, UserQuery.class);
    final var user = this.userService.findOne(query);
    return user.map(UserEntity::toDto);
  }

  @QueryMapping
  public GraphQlPage<UserEntity.Dto> users(@Arguments final FindPageInput input)
      throws JsonProcessingException {
    final var query =
        input.query() == null
            ? UserQuery.builder().build()
            : this.objectMapper.readValue(input.query(), UserQuery.class);
    return GraphQlPage.of(
        this.userService.findPage(
            input.pagination(), GraphQlOrder.parseToModel(input.sort()), query));
  }

  @MutationMapping
  public Optional<UserEntity.Dto> createUser(@Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", UserCommand.TYPE_CREATE);
    return this.userService
        .handle(this.objectMapper.convertValue(args, UserCommand.Create.class))
        .map(UserEntity::toDto);
  }

  @MutationMapping
  public Optional<UserEntity.Dto> updateUser(@Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", UserCommand.TYPE_UPDATE);
    return this.userService
        .handle(this.objectMapper.convertValue(args, UserCommand.Update.class))
        .map(UserEntity::toDto);
  }

  @MutationMapping
  public Optional<UserEntity.Dto> deleteUser(@Argument("targetId") final String targetId) {
    return this.userService.handle(new UserCommand.Delete(targetId)).map(UserEntity::toDto);
  }

  @MutationMapping
  public List<Optional<UserEntity.Dto>> handleUserCommands(
      @Argument("commands") final List<Object> commands) {
    return this.userService.handleAll(
        this.objectMapper.convertValue(commands, new TypeReference<>() {}));
  }
}
