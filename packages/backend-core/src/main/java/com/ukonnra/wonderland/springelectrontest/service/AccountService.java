package com.ukonnra.wonderland.springelectrontest.service;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.AccountDto;
import com.ukonnra.wonderland.springelectrontest.repository.AccountRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccountService
    implements WriteService<Account, Account.Query, Object, AccountRepository, AccountDto> {
  private final AccountRepository repository;

  public AccountService(AccountRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Account> handleCommand(Object command) {
    return null;
  }

  @Override
  public List<AccountDto> convert(Collection<Account> entities) {
    return entities.stream().map(AccountDto::new).toList();
  }

  @Override
  public AccountRepository getRepository() {
    return this.repository;
  }
}
