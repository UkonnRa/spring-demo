package com.ukonnra.wonderland.springelectrontest.repository;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountRepository
    extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {}
