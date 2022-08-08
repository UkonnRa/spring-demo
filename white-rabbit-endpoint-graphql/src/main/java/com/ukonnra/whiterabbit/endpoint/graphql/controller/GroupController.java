package com.ukonnra.whiterabbit.endpoint.graphql.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.auth.AuthUser;
import com.ukonnra.whiterabbit.core.domain.group.GroupCommand;
import com.ukonnra.whiterabbit.core.domain.group.GroupEntity;
import com.ukonnra.whiterabbit.core.domain.group.GroupQuery;
import com.ukonnra.whiterabbit.core.domain.group.GroupService;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserQuery;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.endpoint.graphql.model.FindAllInput;
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
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@Slf4j
@Transactional
@SchemaMapping(typeName = "Group")
public class GroupController {
  private final UserService userService;
  private final GroupService groupService;
  private final ObjectMapper objectMapper;

  public GroupController(
      UserService userService, GroupService groupService, ObjectMapper objectMapper) {
    this.userService = userService;
    this.groupService = groupService;
    this.objectMapper = objectMapper;
  }

  @QueryMapping
  public Optional<GroupEntity.Dto> group(
      @ContextValue final AuthUser authUser, @Argument("query") final String rawQuery)
      throws JsonProcessingException {
    final var query = this.objectMapper.readValue(rawQuery, GroupQuery.class);
    return this.groupService.findOne(authUser, query).map(GroupEntity::toDto);
  }

  @QueryMapping
  public GraphQlPage<GroupEntity, GroupEntity.Dto> groups(
      @ContextValue final AuthUser authUser, @Arguments final FindPageInput input)
      throws JsonProcessingException {
    return new GraphQlPage<>(
        this.groupService.findPage(
            authUser,
            input.pagination(),
            GraphQlOrder.parseToModel(input.sort()),
            this.objectMapper.readValue(input.query(), GroupQuery.class)));
  }

  @MutationMapping
  public Optional<GroupEntity.Dto> createGroup(
      @ContextValue final AuthUser authUser, @Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", GroupCommand.TYPE_CREATE);
    return this.groupService
        .handle(authUser, this.objectMapper.convertValue(args, GroupCommand.Create.class))
        .map(GroupEntity::toDto);
  }

  @MutationMapping
  public Optional<GroupEntity.Dto> updateGroup(
      @ContextValue final AuthUser authUser, @Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", GroupCommand.TYPE_UPDATE);
    return this.groupService
        .handle(authUser, this.objectMapper.convertValue(args, GroupCommand.Update.class))
        .map(GroupEntity::toDto);
  }

  @MutationMapping
  public Optional<GroupEntity.Dto> deleteGroup(
      @ContextValue final AuthUser authUser, @Argument("targetId") final String targetId) {
    return this.groupService
        .handle(authUser, new GroupCommand.Delete(targetId))
        .map(GroupEntity::toDto);
  }

  @MutationMapping
  public List<Optional<GroupEntity.Dto>> handleGroupCommands(
      @ContextValue final AuthUser authUser, @Argument("commands") final List<Object> commands) {
    return this.groupService.handleAll(
        authUser, this.objectMapper.convertValue(commands, new TypeReference<>() {}));
  }

  @SchemaMapping
  public boolean isWriteable(@ContextValue final AuthUser authUser, final GroupEntity.Dto group) {
    return this.groupService
        .findOne(group.id())
        .map(
            g -> {
              try {
                this.groupService.checkWriteable(authUser, g);
                return true;
              } catch (Exception ignored) {
                return false;
              }
            })
        .orElse(false);
  }

  @SchemaMapping
  public List<UserEntity.Dto> admins(
      @ContextValue final AuthUser authUser,
      @Arguments FindAllInput<UserQuery> input,
      final GroupEntity.Dto group) {
    final var query = input.parsedQuery(this.objectMapper).orElse(new UserQuery());
    return this.userService
        .findAll(
            authUser,
            input.parsedSort(),
            input.parsedSize(),
            query.withId(new IdQuery.Multiple(group.admins())))
        .stream()
        .map(UserEntity::toDto)
        .toList();
  }

  @SchemaMapping
  public List<UserEntity.Dto> members(
      @ContextValue final AuthUser authUser,
      @Arguments FindAllInput<UserQuery> input,
      final GroupEntity.Dto group) {
    final var query = input.parsedQuery(this.objectMapper).orElse(new UserQuery());
    return this.userService
        .findAll(
            authUser,
            input.parsedSort(),
            input.parsedSize(),
            query.withId(new IdQuery.Multiple(group.admins())))
        .stream()
        .map(UserEntity::toDto)
        .toList();
  }
}
