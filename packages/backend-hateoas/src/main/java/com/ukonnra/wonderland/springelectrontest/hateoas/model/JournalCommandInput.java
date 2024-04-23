package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ukonnra.wonderland.springelectrontest.entity.JournalCommand;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = JournalCommandInput.Create.class, name = JournalCommand.Create.TYPE),
})
@Schema(
    subTypes = {JournalCommandInput.Create.class},
    discriminatorMapping = {
      @DiscriminatorMapping(
          value = JournalCommand.Create.TYPE,
          schema = JournalCommandInput.Create.class),
    })
public sealed interface JournalCommandInput extends CommandInput<JournalCommand> {
  @Schema(title = "JournalCommandCreate")
  record Create(@Nullable UUID id, String name, String description, String unit, Set<String> tags)
      implements JournalCommandInput {
    @Override
    public JournalCommand generateCommand() {
      return new JournalCommand.Create(this.id, this.name, this.description, this.unit, this.tags);
    }

    @Schema(allowableValues = JournalCommand.Create.TYPE)
    public String getType() {
      return JournalCommand.Create.TYPE;
    }
  }
}
