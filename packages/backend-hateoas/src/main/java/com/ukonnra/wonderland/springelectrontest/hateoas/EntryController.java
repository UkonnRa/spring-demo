package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.ukonnra.wonderland.springelectrontest.entity.AccountDto;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.EntryDto;
import com.ukonnra.wonderland.springelectrontest.entity.JournalDto;
import com.ukonnra.wonderland.springelectrontest.service.AccountService;
import com.ukonnra.wonderland.springelectrontest.service.EntryService;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
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
@RequestMapping(value = "/entries", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Entries", description = "Entry related API")
@Slf4j
@Transactional
public class EntryController {
  private final JournalService journalService;
  private final JournalController journalController;
  private final AccountService accountService;
  private final AccountController accountController;
  private final EntryService entryService;

  public EntryController(
      JournalService journalService,
      JournalController journalController,
      AccountService accountService,
      AccountController accountController,
      EntryService entryService) {
    this.journalService = journalService;
    this.journalController = journalController;
    this.accountService = accountService;
    this.accountController = accountController;
    this.entryService = entryService;
  }

  EntityModel<EntryDto> toEntityModel(EntryDto dto) {
    return EntityModel.of(dto, Link.of("/entries/" + dto.id()));
  }

  @GetMapping
  public CollectionModel<EntityModel<EntryDto>> findAll(
      @RequestParam(name = "filter[id]", required = false)
          @Parameter(description = "Filter Entries by IDs")
          Set<UUID> id,
      @RequestParam(name = "filter[journal]", required = false)
          @Parameter(description = "Filter Entries by Journal IDs")
          Set<UUID> journal,
      @RequestParam(name = "filter[account]", required = false)
          @Parameter(description = "Filter Entries by Account IDs")
          Set<UUID> account,
      @RequestParam(name = "filter[name]", required = false)
          @Parameter(description = "Filter Entries by names with exactly matching")
          Set<String> name,
      @RequestParam(name = "filter[type]", required = false)
          @Parameter(description = "Filter Entries by Entry type")
          @Nullable
          Entry.Type type,
      @RequestParam(name = "filter[start]", required = false)
          @Parameter(description = "Filter Entries after the given date")
          @Nullable
          LocalDate start,
      @RequestParam(name = "filter[end]", required = false)
          @Parameter(description = "Filter Entries before the given date")
          @Nullable
          LocalDate end,
      @RequestParam(name = "filter[tag]", required = false)
          @Parameter(description = "Filter Entries by tags")
          Set<String> tag,
      @RequestParam(name = "filter[fullText]", required = false)
          @Parameter(
              description =
                  "Filter Entries by full-text searching on Field 'name', 'description', 'tags'")
          String fullText) {

    final var query = new Entry.Query();
    query.setId(id);
    query.setJournal(journal);
    query.setAccount(account);
    query.setName(name);
    query.setType(type);
    query.setStart(start);
    query.setEnd(end);
    query.setTag(tag);
    query.setFullText(fullText);

    final var dtos = this.entryService.convert(this.entryService.findAll(query));
    return CollectionModel.of(dtos.stream().map(this::toEntityModel).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<EntryDto>> findById(@PathVariable(name = "id") UUID id) {
    final var query = new Entry.Query();
    query.setId(Set.of(id));

    final var dto = this.entryService.convert(this.entryService.findOne(query));
    return ResponseEntity.of(dto.map(this::toEntityModel));
  }

  @GetMapping("/{id}/accounts")
  public CollectionModel<EntityModel<AccountDto>> findRelatedAccounts(
      @PathVariable(name = "id") UUID id) {
    final var query = new Entry.Query();
    query.setId(Set.of(id));

    final var accounts =
        this.entryService.findOne(query).stream()
            .flatMap(entry -> entry.getItems().stream().map(Entry.Item::getAccount))
            .distinct()
            .toList();
    final var dtos = this.accountService.convert(accounts);

    return CollectionModel.of(dtos.stream().map(this.accountController::toEntityModel).toList());
  }

  @GetMapping("/{id}/journal")
  public ResponseEntity<EntityModel<JournalDto>> findRelatedJournal(
      @PathVariable(name = "id") UUID id) {
    final var query = new Entry.Query();
    query.setId(Set.of(id));

    return ResponseEntity.of(
        this.entryService
            .findOne(query)
            .flatMap(account -> this.journalService.convert(account.getJournal()))
            .map(this.journalController::toEntityModel));
  }
}
