package com.ukonnra.wonderland.springelectrontest.repository;

import com.ukonnra.wonderland.springelectrontest.entity.EntryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryItemRepository extends JpaRepository<EntryItem, EntryItem.EntryItemId> {}
