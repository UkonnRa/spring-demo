package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.entity.JournalDto;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  EntityModel<JournalDto> toEntityModel(JournalDto dto) {
    return EntityModel.of(dto, Link.of("/journals/" + dto.id()));
  }

  @GetMapping
  public CollectionModel<EntityModel<JournalDto>> findAll(
      @RequestParam(name = "filter[id]", required = false)
          @Parameter(description = "Filter Journals by IDs")
          Set<UUID> id,
      @RequestParam(name = "filter[name]", required = false)
          @Parameter(description = "Filter Journals by names with exactly matching")
          Set<String> name,
      @RequestParam(name = "filter[unit]", required = false)
          @Parameter(description = "Filter Journals by units")
          Set<String> unit,
      @RequestParam(name = "filter[tag]", required = false)
          @Parameter(description = "Filter Journals containing any of the given tags")
          Set<String> tag,
      @RequestParam(name = "filter[fullText]", required = false)
          @Parameter(
              description =
                  "Filter Journals by full-text searching on Field 'name', 'description', 'tags'")
          String fullText) {

    final var query = new Journal.Query();
    query.setId(id);
    query.setName(name);
    query.setUnit(unit);
    query.setTag(tag);
    query.setFullText(fullText);

    final var dtos = this.journalService.convert(this.journalService.findAll(query));
    return CollectionModel.of(dtos.stream().map(this::toEntityModel).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<JournalDto>> findById(@PathVariable(name = "id") UUID id) {
    final var query = new Journal.Query();
    query.setId(Set.of(id));

    final var dto = this.journalService.convert(this.journalService.findOne(query));
    return ResponseEntity.of(dto.map(this::toEntityModel));
  }
}
