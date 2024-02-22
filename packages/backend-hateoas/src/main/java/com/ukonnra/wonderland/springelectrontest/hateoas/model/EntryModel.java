package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.EntryDto;
import com.ukonnra.wonderland.springelectrontest.entity.EntryState;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.core.Relation;

@Getter
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = EntryModel.Record.class, name = "RECORD"),
  @JsonSubTypes.Type(value = EntryModel.Check.class, name = "CHECK")
})
@Schema(
    subTypes = {EntryModel.Record.class, EntryModel.Check.class},
    discriminatorMapping = {
      @DiscriminatorMapping(value = "RECORD", schema = EntryModel.Record.class),
      @DiscriminatorMapping(value = "CHECK", schema = EntryModel.Check.class)
    })
@Relation(collectionRelation = "entries")
public abstract sealed class EntryModel extends AbstractModel<EntryDto, EntryModel> {
  private final UUID id;
  private final Instant createdDate;
  private final int version;
  private final UUID journalId;
  private final String name;
  private final String description;
  private final LocalDate date;
  private final Set<String> tags;
  private final Set<EntryDto.Item> items;

  @JsonIgnore
  @Transient
  public Set<UUID> getAccountIds() {
    return this.items.stream().map(EntryDto.Item::accountId).collect(Collectors.toSet());
  }

  protected EntryModel(final EntryDto dto, @Nullable Link... links) {
    super(dto);
    if (links == null || links.length == 0) {
      this.add(Link.of("/entries/" + dto.id()));
    } else {
      this.add(links);
    }

    this.id = dto.id();
    this.createdDate = dto.createdDate();
    this.version = dto.version();
    this.journalId = dto.journalId();
    this.name = dto.name();
    this.description = dto.description();
    this.date = dto.date();
    this.tags = dto.tags();
    this.items = dto.items();
  }

  @JsonProperty
  @Schema(hidden = true)
  public abstract Entry.Type getType();

  @Getter
  @EqualsAndHashCode(callSuper = true)
  @Schema(name = "EntryModelRecord")
  @SchemaProperty(name = "type", schema = @Schema(allowableValues = "RECORD"))
  @Relation(collectionRelation = "entries")
  public static final class Record extends EntryModel {
    private final EntryState state;

    public Record(EntryDto.Record dto, @Nullable Link... links) {
      super(dto, links);
      this.state = dto.state();
    }

    @Override
    @JsonProperty
    public Entry.Type getType() {
      return Entry.Type.RECORD;
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = true)
  @Schema(name = "EntryModelCheck")
  @SchemaProperty(name = "type", schema = @Schema(allowableValues = "CHECK"))
  @Relation(collectionRelation = "entries")
  public static final class Check extends EntryModel {
    private final Map<UUID, EntryState> state;

    public Check(EntryDto.Check dto, @Nullable Link... links) {
      super(dto, links);
      this.state = dto.state();
    }

    @Override
    @JsonProperty
    public Entry.Type getType() {
      return Entry.Type.CHECK;
    }
  }
}
