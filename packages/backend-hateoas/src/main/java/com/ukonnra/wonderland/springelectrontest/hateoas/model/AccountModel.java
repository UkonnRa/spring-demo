package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.AccountDto;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.core.Relation;

@Getter
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "accounts")
public class AccountModel extends AbstractModel<AccountDto, AccountModel> {
  private final UUID id;
  private final Instant createdDate;
  private final int version;
  private final UUID journalId;
  private final String name;
  private final String description;
  private final String unit;
  private final Account.Type type;
  private final Set<String> tags;

  public AccountModel(AccountDto dto, @Nullable Link... links) {
    super(dto);
    if (links == null || links.length == 0) {
      this.add(Link.of("/accounts/" + dto.id()));
    } else {
      this.add(links);
    }

    this.id = dto.id();
    this.createdDate = dto.createdDate();
    this.version = dto.version();
    this.journalId = dto.journalId();
    this.name = dto.name();
    this.description = dto.description();
    this.unit = dto.unit();
    this.type = dto.type();
    this.tags = dto.tags();
  }
}
