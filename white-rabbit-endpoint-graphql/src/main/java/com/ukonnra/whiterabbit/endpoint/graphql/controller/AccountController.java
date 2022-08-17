package com.ukonnra.whiterabbit.endpoint.graphql.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.account.AccountCommand;
import com.ukonnra.whiterabbit.core.domain.account.AccountEntity;
import com.ukonnra.whiterabbit.core.domain.account.AccountQuery;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
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
@SchemaMapping(typeName = AccountController.TYPE)
public class AccountController {
  public static final String TYPE = "Account";

  private final AccountService accountService;
  private final JournalService journalService;
  private final ObjectMapper objectMapper;

  public AccountController(
      AccountService accountService, JournalService journalService, ObjectMapper objectMapper) {
    this.accountService = accountService;
    this.journalService = journalService;
    this.objectMapper = objectMapper;
  }

  @QueryMapping
  public Optional<AccountEntity.Dto> account(@Argument("query") final String rawQuery)
      throws JsonProcessingException {
    final var query = this.objectMapper.readValue(rawQuery, AccountQuery.class);
    return this.accountService.findOne(query).map(AccountEntity::toDto);
  }

  @QueryMapping
  public GraphQlPage<AccountEntity.Dto> accounts(@Arguments final FindPageInput input)
      throws JsonProcessingException {
    final var query =
        input.query() == null
            ? AccountQuery.builder().build()
            : this.objectMapper.readValue(input.query(), AccountQuery.class);

    return GraphQlPage.of(
        this.accountService.findPage(
            input.pagination(), GraphQlOrder.parseToModel(input.sort()), query));
  }

  @MutationMapping
  public Optional<AccountEntity.Dto> createAccount(@Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", AccountCommand.TYPE_CREATE);
    return this.accountService
        .handle(this.objectMapper.convertValue(args, AccountCommand.Create.class))
        .map(AccountEntity::toDto);
  }

  @MutationMapping
  public Optional<AccountEntity.Dto> updateAccount(@Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", AccountCommand.TYPE_UPDATE);
    return this.accountService
        .handle(this.objectMapper.convertValue(args, AccountCommand.Update.class))
        .map(AccountEntity::toDto);
  }

  @MutationMapping
  public Optional<AccountEntity.Dto> deleteAccount(@Argument("targetId") final String targetId) {
    return this.accountService
        .handle(new AccountCommand.Delete(targetId))
        .map(AccountEntity::toDto);
  }

  @MutationMapping
  public List<Optional<AccountEntity.Dto>> handleAccountCommands(
      @Argument("commands") final List<Object> commands) {
    return this.accountService.handleAll(
        this.objectMapper.convertValue(commands, new TypeReference<>() {}));
  }

  @SchemaMapping
  public Optional<JournalEntity.Dto> journal(final AccountEntity.Dto account) {
    return this.journalService.findOne(account.journal()).map(JournalEntity::toDto);
  }
}
