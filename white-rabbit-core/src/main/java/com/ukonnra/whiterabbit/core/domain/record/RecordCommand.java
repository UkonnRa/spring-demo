package com.ukonnra.whiterabbit.core.domain.record;

import com.ukonnra.whiterabbit.core.Command;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import lombok.With;
import org.springframework.lang.Nullable;

public sealed interface RecordCommand extends Command<RecordCommand>
    permits RecordCommand.Create, RecordCommand.Delete, RecordCommand.Update {
  String TYPE_CREATE = "RecordCommandCreate";
  String TYPE_UPDATE = "RecordCommandUpdate";
  String TYPE_DELETE = "RecordCommandDelete";

  record Create(
      @With @Nullable String targetId,
      UUID journal,
      String name,
      String description,
      RecordType recordType,
      LocalDate date,
      Set<String> tags,
      Set<RecordItemValue.Dto> items)
      implements RecordCommand {
    @Override
    public String type() {
      return TYPE_CREATE;
    }
  }

  record Update(
      @With String targetId,
      @Nullable String name,
      @Nullable String description,
      @Nullable RecordType recordType,
      @Nullable LocalDate date,
      @Nullable Set<String> tags,
      @Nullable Set<RecordItemValue.Dto> items)
      implements RecordCommand {
    @Override
    public String type() {
      return TYPE_UPDATE;
    }
  }

  record Delete(@With String targetId) implements RecordCommand {
    @Override
    public String type() {
      return TYPE_DELETE;
    }
  }
}
