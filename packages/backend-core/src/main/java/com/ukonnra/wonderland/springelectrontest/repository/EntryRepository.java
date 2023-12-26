package com.ukonnra.wonderland.springelectrontest.repository;

import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EntryRepository
    extends JpaRepository<Entry, UUID>, JpaSpecificationExecutor<Entry> {}
