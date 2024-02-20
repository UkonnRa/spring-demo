package com.ukonnra.wonderland.springelectrontest.service;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.AccountDto;
import com.ukonnra.wonderland.springelectrontest.repository.AccountRepository;
import jakarta.validation.Validator;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

public interface AccountService
    extends WriteService<Account, Account.Query, Object, AccountRepository, AccountDto> {

  @Override
  default List<Account> handleCommand(Object command) {
    return null;
  }

  @Override
  default List<AccountDto> convert(Collection<Account> entities) {
    return entities.stream().map(AccountDto::new).toList();
  }

  @Service
  class Impl implements AccountService {
    private final AccountRepository repository;
    private final Validator validator;

    public Impl(AccountRepository repository, Validator validator) {
      this.repository = repository;
      this.validator = validator;
    }

    @Override
    public AccountRepository getRepository() {
      return this.repository;
    }

    @Override
    public Validator getValidator() {
      return this.validator;
    }
  }
}
