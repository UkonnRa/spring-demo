package com.ukonnra.whiterabbit.endpoint.graphql.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.account.AccountEntity;
import com.ukonnra.whiterabbit.core.domain.account.AccountQuery;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.journal.JournalCommand;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalQuery;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.record.RecordEntity;
import com.ukonnra.whiterabbit.core.domain.record.RecordQuery;
import com.ukonnra.whiterabbit.core.domain.record.RecordService;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@Slf4j
@Transactional
@SchemaMapping(typeName = JournalController.TYPE)
public class JournalController {
  public static final String TYPE = "Journal";
  public static final String TYPE_ACCESS_ITEM = "AccessItem";

  private final JournalService journalService;
  private final ObjectMapper objectMapper;
  private final AccountService accountService;
  private final RecordService recordService;
  private final UserRepository userRepository;

  public JournalController(
      JournalService journalService,
      ObjectMapper objectMapper,
      AccountService accountService,
      RecordService recordService,
      UserRepository userRepository) {
    this.journalService = journalService;
    this.objectMapper = objectMapper;
    this.accountService = accountService;
    this.recordService = recordService;
    this.userRepository = userRepository;
  }

  @QueryMapping
  public Optional<JournalEntity.Dto> journal(@Argument("query") final String rawQuery)
      throws JsonProcessingException {
    final var query = this.objectMapper.readValue(rawQuery, JournalQuery.class);
    return this.journalService.findOne(query).map(JournalEntity::toDto);
  }

  @QueryMapping
  public GraphQlPage<JournalEntity.Dto> journals(@Arguments final FindPageInput input)
      throws JsonProcessingException {
    final var query =
        input.query() == null
            ? JournalQuery.builder().build()
            : this.objectMapper.readValue(input.query(), JournalQuery.class);

    return GraphQlPage.of(
        this.journalService.findPage(
            input.pagination(), GraphQlOrder.parseToModel(input.sort()), query));
  }

  @MutationMapping
  public Optional<JournalEntity.Dto> createJournal(@Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", JournalCommand.TYPE_CREATE);
    return this.journalService
        .handle(this.objectMapper.convertValue(args, JournalCommand.Create.class))
        .map(JournalEntity::toDto);
  }

  @MutationMapping
  public Optional<JournalEntity.Dto> updateJournal(@Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", JournalCommand.TYPE_UPDATE);
    return this.journalService
        .handle(this.objectMapper.convertValue(args, JournalCommand.Update.class))
        .map(JournalEntity::toDto);
  }

  @MutationMapping
  public Optional<JournalEntity.Dto> deleteJournal(@Argument("targetId") final String targetId) {
    return this.journalService
        .handle(new JournalCommand.Delete(targetId))
        .map(JournalEntity::toDto);
  }

  @MutationMapping
  public List<Optional<JournalEntity.Dto>> handleJournalCommands(
      @AuthenticationPrincipal Authentication authentication,
      @Argument("commands") final List<Object> commands) {
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return this.journalService.handleAll(
        this.objectMapper.convertValue(commands, new TypeReference<>() {}));
  }

  @SchemaMapping
  public boolean isWriteable(final JournalEntity.Dto journal) {
    return this.journalService
        .findOne(journal.id())
        .map(
            g -> {
              try {
                this.journalService.checkWriteable(g);
                return true;
              } catch (Exception ignored) {
                return false;
              }
            })
        .orElse(false);
  }

  @SchemaMapping
  public boolean isAdmin(
      @AuthenticationPrincipal Authentication authentication, final JournalEntity.Dto journal) {
    final var authUser =
        this.userRepository.findOne(
            QUserEntity.userEntity.authIds.any().tokenValue.eq(authentication.getName()));
    return this.journalService
        .findOne(journal.id())
        .flatMap(
            j ->
                authUser.map(
                    user -> this.journalService.isContainingUser(j.getAdmins(), user.getId())))
        .orElse(false);
  }

  @SchemaMapping
  public GraphQlPage<AccountEntity.Dto> accounts(
      final JournalEntity.Dto journal, @Arguments final FindPageInput input)
      throws JsonProcessingException {
    final var query =
        input.query() == null
            ? AccountQuery.builder().journal(journal.id()).build()
            : this.objectMapper
                .readValue(input.query(), AccountQuery.class)
                .withJournal(journal.id());

    return GraphQlPage.of(
        this.accountService.findPage(
            input.pagination(), GraphQlOrder.parseToModel(input.sort()), query));
  }

  @SchemaMapping
  public GraphQlPage<RecordEntity.Dto> records(
      final JournalEntity.Dto journal, @Arguments final FindPageInput input)
      throws JsonProcessingException {
    final var query =
        input.query() == null
            ? RecordQuery.builder().journal(journal.id()).build()
            : this.objectMapper
                .readValue(input.query(), RecordQuery.class)
                .withJournal(journal.id());

    return GraphQlPage.of(
        this.recordService.findPage(
            input.pagination(), GraphQlOrder.parseToModel(input.sort()), query));
  }
}
