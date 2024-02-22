package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.entity.JournalDto;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalModel;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/journals", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Journals", description = "Journal related API")
@Slf4j
@Transactional
public class JournalController {
  private final JournalService journalService;

  public JournalController(JournalService journalService) {
    this.journalService = journalService;
  }

  JournalModel toEntityModel(JournalDto dto, Link... links) {
    return new JournalModel(dto, links);
  }

  @GetMapping
  public CollectionModel<JournalModel> findAll(@ParameterObject JournalArgs.FindAll args) {

    final var query = new Journal.Query();
    query.setId(args.filter().id());
    query.setName(args.filter().name());
    query.setUnit(args.filter().unit());
    query.setTag(args.filter().tag());
    query.setFullText(args.filter().fullText());

    final var dtos = this.journalService.convert(this.journalService.findAll(query));
    return CollectionModel.of(dtos.stream().map(this::toEntityModel).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<JournalModel> findById(@PathVariable(name = "id") UUID id) {
    final var query = new Journal.Query();
    query.setId(Set.of(id));

    final var dto = this.journalService.convert(this.journalService.findOne(query));
    return ResponseEntity.of(dto.map(this::toEntityModel));
  }
}
