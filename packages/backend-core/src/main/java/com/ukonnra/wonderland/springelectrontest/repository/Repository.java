package com.ukonnra.wonderland.springelectrontest.repository;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface Repository<E extends AbstractEntity>
    extends JpaRepository<E, UUID>, JpaSpecificationExecutor<E> {}
