package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.entity.JournalCommand;
import com.ukonnra.wonderland.springelectrontest.entity.JournalDto;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalAttributes;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalCommandInput;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalsModel;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/journals", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Journals", description = "Journal related API")
@Slf4j
@Transactional
public class JournalController {
  private final ObjectMapper objectMapper;
  private final JournalService journalService;

  public JournalController(ObjectMapper objectMapper, JournalService journalService) {
    this.objectMapper = objectMapper;
    this.journalService = journalService;
  }

  JournalModel toEntityModel(JournalDto dto, Link... links) {
    return new JournalModel(dto, links);
  }

  @GetMapping
  public JournalsModel findAll(@ParameterObject JournalArgs.FindAll args) {

    final var query = new Journal.Query();
    query.setId(args.filter().id());
    query.setName(args.filter().name());
    query.setUnit(args.filter().unit());
    query.setTag(args.filter().tag());
    query.setFullText(args.filter().fullText());

    final var dtos = this.journalService.convert(this.journalService.findAll(query));
    final var models = dtos.stream().map(this::toEntityModel).toList();
    return new JournalsModel(models);
  }

  @GetMapping("/{id}")
  public ResponseEntity<JournalAttributes> findById(@PathVariable(name = "id") UUID id) {
    final var query = new Journal.Query();
    query.setId(Set.of(id));
    log.info("Query: {}", query);
    return ResponseEntity.noContent().build();
  }

  @PostMapping
  public ResponseEntity<JournalModel> create(@RequestBody JournalCommandInput.Create command) {
    final var result =
        this.journalService.handleCommand(command.generateCommand()).stream().findFirst();
    return ResponseEntity.of(this.journalService.convert(result).map(this::toEntityModel));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<JournalModel> update(
      @PathVariable(name = "id") UUID id, @RequestBody Map<String, Object> body) {
    body.put("id", id);
    final var command = this.objectMapper.convertValue(body, JournalCommand.Update.class);
    final var result = this.journalService.handleCommand(command).stream().findFirst();
    return ResponseEntity.of(this.journalService.convert(result).map(this::toEntityModel));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable(name = "id") UUID id) {
    this.journalService.handleCommand(new JournalCommand.Delete(Set.of(id)));
    return ResponseEntity.noContent().build();
  }
}
