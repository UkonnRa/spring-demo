package com.ukonnra.whiterabbit.endpoint.graphql.controller;

import com.ukonnra.whiterabbit.core.domain.account.AccountEntity;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.record.RecordItemValue;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@Slf4j
@Transactional
@SchemaMapping(typeName = RecordItemController.TYPE)
public class RecordItemController {
  public static final String TYPE = "RecordItem";
  private final AccountService accountService;

  public RecordItemController(AccountService accountService) {
    this.accountService = accountService;
  }

  @SchemaMapping
  public Optional<AccountEntity.Dto> account(final RecordItemValue.Dto item) {
    return this.accountService.findOne(item.account()).map(AccountEntity::toDto);
  }
}
