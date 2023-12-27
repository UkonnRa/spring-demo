package com.ukonnra.wonderland.springelectrontest.configuration;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity;
import com.ukonnra.wonderland.springelectrontest.repository.Repository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan(basePackageClasses = AbstractEntity.class)
@EnableJpaRepositories(basePackageClasses = Repository.class)
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfiguration {
  @Bean
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }
}
