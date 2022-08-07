package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.account.AccountEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.record.RecordEntity;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.springframework.lang.Nullable;

public abstract class CoreError extends RuntimeException {
  public abstract String getType();

  protected CoreError(String message) {
    super(message);
  }

  @Getter
  public static class Errors extends CoreError {
    private final Set<CoreError> errors;

    public Errors(Set<CoreError> errors) {
      super(
          String.format(
              "There are %d error(s) thrown, please check them in detail", errors.size()));
      this.errors = errors;
    }

    @Override
    public String getType() {
      return "Errors";
    }
  }

  @Getter
  public static class NotFound extends CoreError {
    private final String entityType;

    private final String entityId;

    public NotFound(String entityType, String entityId) {
      super(String.format("Type[%s, id=%s] not found", entityType, entityId));
      this.entityType = entityType;
      this.entityId = entityId;
    }

    @Override
    public String getType() {
      return "NotFound";
    }
  }

  @Getter
  public static class InvalidCommand extends CoreError {
    private final String entityType;
    private final String commandType;

    public InvalidCommand(String entityType, String commandType) {
      super(String.format("Command[%s] is invalid for Type[%s]", commandType, entityType));
      this.entityType = entityType;
      this.commandType = commandType;
    }

    @Override
    public String getType() {
      return "InvalidCommand";
    }
  }

  @Getter
  public static class NoPermission extends CoreError {
    private final String entityType;
    private final String entityId;
    private final Permission permission;

    public static NoPermission read(String entityType, @Nullable String entityId) {
      return new NoPermission(entityType, entityId, Permission.READ);
    }

    public static NoPermission write(String entityType, @Nullable String entityId) {
      return new NoPermission(entityType, entityId, Permission.WRITE);
    }

    private NoPermission(String entityType, @Nullable String entityId, Permission permission) {
      super(
          String.format(
              "Permission[%s] not found for Type[%s, id=%s]", permission, entityType, entityId));
      this.entityType = entityType;
      this.entityId = entityId == null ? "null" : entityId;
      this.permission = permission;
    }

    @Override
    public String getType() {
      return "NoPermission";
    }

    private enum Permission {
      READ,
      WRITE
    }
  }

  @Getter
  public static class AlreadyExist extends CoreError {
    private final String entityType;
    private final String field;
    private final String value;

    public AlreadyExist(String entityType, String field, String value) {
      super(
          String.format(
              "Type[%s] with Field[%s, value=%s] is already existing", entityType, field, value));
      this.entityType = entityType;
      this.field = field;
      this.value = value;
    }

    @Override
    public String getType() {
      return "AlreadyExist";
    }
  }

  @Getter
  public static class AlreadyArchived extends CoreError {
    private final String entityType;
    private final UUID id;

    public AlreadyArchived(String entityType, UUID id) {
      super(String.format("Type[%s, id=%s] is already archived", entityType, id));
      this.entityType = entityType;
      this.id = id;
    }

    @Override
    public String getType() {
      return "AlreadyExist";
    }
  }

  @Getter
  public static class RecordUnitNotMatch extends CoreError {
    private final UUID recordId;

    private final UUID accountId;

    public RecordUnitNotMatch(final RecordEntity record, final UUID accountId) {
      super(
          String.format(
              "RecordItem[account=%s] in Record[%s] needs to provide Field[price]"
                  + " since the account's unit does not match the journal's",
              accountId, record.getId()));
      this.recordId = record.getId();
      this.accountId = accountId;
    }

    @Override
    public String getType() {
      return "RecordUnitNotMatch";
    }
  }

  @Getter
  public static class RecordWithMultipleEmptyItems extends CoreError {
    private final UUID recordId;

    private final Set<UUID> accountIds;

    public RecordWithMultipleEmptyItems(final RecordEntity record, final Set<UUID> accountIds) {
      super(
          String.format(
              "Field[amount] are empty in RecordItems[accounts=%s] in Record[%s],"
                  + " only at most ONE RecordItem can have the empty Field[amount]",
              accountIds, record.getId()));
      this.recordId = record.getId();
      this.accountIds = accountIds;
    }

    @Override
    public String getType() {
      return "RecordWithMultipleEmptyItems";
    }
  }

  @Getter
  public static class AccountNotInJournal extends CoreError {
    private final UUID journalId;

    private final UUID accountId;

    public AccountNotInJournal(final JournalEntity journal, final AccountEntity account) {
      super(String.format("Account[%s] does not in Journal[%s]", account.getId(), journal.getId()));
      this.journalId = journal.getId();
      this.accountId = account.getId();
    }

    @Override
    public String getType() {
      return "AccountNotInJournal";
    }
  }
}
