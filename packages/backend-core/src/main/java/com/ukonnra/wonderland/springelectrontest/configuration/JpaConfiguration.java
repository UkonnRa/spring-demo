package com.ukonnra.wonderland.springelectrontest.configuration;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity;
import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity_;
import com.ukonnra.wonderland.springelectrontest.entity.Account_;
import com.ukonnra.wonderland.springelectrontest.entity.EntryItem_;
import com.ukonnra.wonderland.springelectrontest.entity.Entry_;
import com.ukonnra.wonderland.springelectrontest.entity.Journal_;
import com.ukonnra.wonderland.springelectrontest.repository.Repository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.domain.AbstractPersistable_;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ReflectionUtils;

@Configuration
@EntityScan(basePackageClasses = AbstractEntity.class)
@EnableJpaRepositories(basePackageClasses = Repository.class)
@EnableJpaAuditing
@EnableTransactionManagement
@ImportRuntimeHints(JpaConfiguration.RuntimeHints.class)
public class JpaConfiguration {
  @Bean
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }

  public static class RuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(
        org.springframework.aot.hint.RuntimeHints hints, ClassLoader classLoader) {
      // hibernate-jpamodelgen uses Reflection to work
      final var entries =
          List.of(
              Map.entry(AbstractPersistable_.ID, AbstractPersistable_.class),
              Map.entry(AbstractEntity_.CREATED_DATE, AbstractEntity_.class),
              Map.entry(AbstractEntity_.VERSION, AbstractEntity_.class),
              Map.entry(Journal_.UNIT, Journal_.class),
              Map.entry(Journal_.NAME, Journal_.class),
              Map.entry(Journal_.DESCRIPTION, Journal_.class),
              Map.entry(Journal_.TAGS, Journal_.class),
              Map.entry(Account_.UNIT, Account_.class),
              Map.entry(Account_.JOURNAL, Account_.class),
              Map.entry(Account_.NAME, Account_.class),
              Map.entry(Account_.DESCRIPTION, Account_.class),
              Map.entry(Account_.TYPE, Account_.class),
              Map.entry(Account_.TAGS, Account_.class),
              Map.entry(Entry_.DATE, Entry_.class),
              Map.entry(Entry_.JOURNAL, Entry_.class),
              Map.entry(Entry_.NAME, Entry_.class),
              Map.entry(Entry_.DESCRIPTION, Entry_.class),
              Map.entry(Entry_.TYPE, Entry_.class),
              Map.entry(Entry_.ITEMS, Entry_.class),
              Map.entry(Entry_.TAGS, Entry_.class),
              Map.entry(EntryItem_.ENTRY, EntryItem_.class),
              Map.entry(EntryItem_.AMOUNT, EntryItem_.class),
              Map.entry(EntryItem_.PRICE, EntryItem_.class),
              Map.entry(EntryItem_.ID, EntryItem_.class),
              Map.entry(EntryItem_.ACCOUNT, EntryItem_.class));

      for (final var entry : entries) {
        final var field = ReflectionUtils.findField(entry.getValue(), entry.getKey());
        if (field != null) {
          hints.reflection().registerField(field);
        }
      }

      // https://github.com/FasterXML/jackson-databind/issues/4299
      hints
          .reflection()
          .registerTypes(
              TypeReference.listOf(
                  ArrayList.class,
                  LinkedList.class,
                  HashSet.class,
                  TreeSet.class,
                  ConcurrentHashMap.class,
                  LinkedHashMap.class,
                  TreeMap.class),
              TypeHint.builtWith(
                  MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS));
    }
  }
}
