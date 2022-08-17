package com.ukonnra.whiterabbit.endpoint.graphql.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.record.RecordCommand;
import com.ukonnra.whiterabbit.core.domain.record.RecordEntity;
import com.ukonnra.whiterabbit.core.domain.record.RecordQuery;
import com.ukonnra.whiterabbit.core.domain.record.RecordService;
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
@SchemaMapping(typeName = RecordController.TYPE)
public class RecordController {
  public static final String TYPE = "Record";

  private final RecordService recordService;
  private final JournalService journalService;
  private final ObjectMapper objectMapper;

  public RecordController(
      RecordService recordService, JournalService journalService, ObjectMapper objectMapper) {
    this.recordService = recordService;
    this.journalService = journalService;
    this.objectMapper = objectMapper;
  }

  @QueryMapping
  public Optional<RecordEntity.Dto> record(@Argument("query") final String rawQuery)
      throws JsonProcessingException {
    final var query = this.objectMapper.readValue(rawQuery, RecordQuery.class);
    return this.recordService.findOne(query).map(RecordEntity::toDto);
  }

  @QueryMapping
  public GraphQlPage<RecordEntity.Dto> records(@Arguments final FindPageInput input)
      throws JsonProcessingException {
    final var query =
        input.query() == null
            ? RecordQuery.builder().build()
            : this.objectMapper.readValue(input.query(), RecordQuery.class);

    return GraphQlPage.of(
        this.recordService.findPage(
            input.pagination(), GraphQlOrder.parseToModel(input.sort()), query));
  }

  @MutationMapping
  public Optional<RecordEntity.Dto> createRecord(@Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", RecordCommand.TYPE_CREATE);
    return this.recordService
        .handle(this.objectMapper.convertValue(args, RecordCommand.Create.class))
        .map(RecordEntity::toDto);
  }

  @MutationMapping
  public Optional<RecordEntity.Dto> updateRecord(@Argument Map<String, Object> args) {
    args = new HashMap<>(args);
    args.put("type", RecordCommand.TYPE_UPDATE);
    return this.recordService
        .handle(this.objectMapper.convertValue(args, RecordCommand.Update.class))
        .map(RecordEntity::toDto);
  }

  @MutationMapping
  public Optional<RecordEntity.Dto> deleteRecord(@Argument("targetId") final String targetId) {
    return this.recordService.handle(new RecordCommand.Delete(targetId)).map(RecordEntity::toDto);
  }

  @MutationMapping
  public List<Optional<RecordEntity.Dto>> handleRecordCommands(
      @Argument("commands") final List<Object> commands) {
    return this.recordService.handleAll(
        this.objectMapper.convertValue(commands, new TypeReference<>() {}));
  }

  @SchemaMapping
  public Optional<JournalEntity.Dto> journal(final RecordEntity.Dto record) {
    return this.journalService.findOne(record.journal()).map(JournalEntity::toDto);
  }
}
