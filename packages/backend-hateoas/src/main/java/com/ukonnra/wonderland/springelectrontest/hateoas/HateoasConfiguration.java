package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.ukonnra.wonderland.springelectrontest.CoreConfiguration;
import com.ukonnra.wonderland.springelectrontest.entity.EntryDto;
import com.ukonnra.wonderland.springelectrontest.entity.EntryState;
import com.ukonnra.wonderland.springelectrontest.hateoas.controller.AccountArgs;
import com.ukonnra.wonderland.springelectrontest.hateoas.controller.EntryArgs;
import com.ukonnra.wonderland.springelectrontest.hateoas.controller.HierarchyReportArgs;
import com.ukonnra.wonderland.springelectrontest.hateoas.controller.JournalArgs;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Import({CoreConfiguration.class, BuildProperties.class})
@EnableConfigurationProperties({
  HateoasProperties.class,
})
@ComponentScan(basePackageClasses = HateoasConfiguration.class)
@RegisterReflectionForBinding({
  JournalArgs.FindAll.class,
  AccountArgs.FindAll.class,
  AccountArgs.FindById.class,
  EntryArgs.FindAll.class,
  EntryArgs.FindById.class,
  HierarchyReportArgs.FindAll.class,
  HierarchyReportArgs.FindById.class,
})
public class HateoasConfiguration implements WebMvcConfigurer {
  static {
    final var converters = ModelConverters.getInstance();

    final var entryStateValidSchema =
        converters
            .readAllAsResolvedSchema(EntryState.Valid.class)
            .schema
            .addProperty("type", new StringSchema().addEnumItem("VALID"));
    final var entryStateInvalidSchema =
        converters
            .readAllAsResolvedSchema(EntryState.Invalid.class)
            .schema
            .addProperty("type", new StringSchema().addEnumItem("INVALID"));

    SpringDocUtils.getConfig()
        .replaceWithSchema(
            EntryDto.Item.class, converters.readAllAsResolvedSchema(EntryDto.Item.class).schema)
        .replaceWithSchema(
            EntryState.class,
            new Schema<EntryState>()
                .oneOf(List.of(entryStateValidSchema, entryStateInvalidSchema)));
  }

  private final HateoasQueryParamResolver queryParamResolver;

  public HateoasConfiguration(HateoasQueryParamResolver queryParamResolver) {
    this.queryParamResolver = queryParamResolver;
  }

  @Bean
  public OpenAPI openapi(
      final HateoasProperties properties, final BuildProperties buildProperties) {
    return new OpenAPI()
        .info(new Info().title("Spring Electron Test API").version(buildProperties.getVersion()))
        .addServersItem(new Server().url(properties.baseUrl()));
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(this.queryParamResolver);
  }
}
