package com.ukonnra.wonderland.springelectrontest.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.ukonnra.wonderland.springelectrontest.service.HierarchyReportService;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding({
  HierarchyReportService.AccountIndex.class,
  HierarchyReportService.PrefixIndex.class,
})
public class JsonConfiguration {
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder ->
        builder
            .featuresToEnable(
                JsonGenerator.Feature.IGNORE_UNKNOWN,
                DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL,
                DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .serializationInclusion(JsonInclude.Include.NON_NULL);
  }
}
