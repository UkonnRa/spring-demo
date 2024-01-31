package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.ukonnra.wonderland.springelectrontest.CoreConfiguration;
import com.ukonnra.wonderland.springelectrontest.entity.EntryDto;
import com.ukonnra.wonderland.springelectrontest.entity.EntryState;
import com.ukonnra.wonderland.springelectrontest.hateoas.controller.JournalController;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.AbstractModel;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CoreConfiguration.class)
@EnableConfigurationProperties(HateoasProperties.class)
@ComponentScan(basePackageClasses = {AbstractModel.class, JournalController.class})
public class HateoasConfiguration {
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

  @Bean
  public OpenAPI openapi(
      @Value("${server.port}") int port,
      final HateoasProperties properties,
      final BuildProperties buildProperties) {
    return new OpenAPI()
        .info(new Info().title("Spring Electron Test API").version(buildProperties.getVersion()))
        .addServersItem(
            new Server().url(String.format("http://%s:%d", properties.domainName(), port)));
  }
}
