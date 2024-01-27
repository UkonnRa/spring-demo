package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.ukonnra.wonderland.springelectrontest.CoreConfiguration;
import com.ukonnra.wonderland.springelectrontest.entity.EntryDto;
import com.ukonnra.wonderland.springelectrontest.entity.EntryState;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Map;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CoreConfiguration.class)
@EnableConfigurationProperties(HateoasProperties.class)
@ComponentScan(basePackageClasses = HateoasConfiguration.class)
public class HateoasConfiguration {
  static {
    final var converters = ModelConverters.getInstance();

    final var entryRecordSchema = converters.readAllAsResolvedSchema(EntryDto.Record.class).schema;
    final var entryCheckSchema = converters.readAllAsResolvedSchema(EntryDto.Check.class).schema;

    SpringDocUtils.getConfig()
        .replaceWithSchema(
            EntryDto.class,
            new Schema<EntryDto>()
                .discriminator(
                    new Discriminator()
                        .propertyName("type")
                        .mapping(
                            Map.of(
                                "RECORD", entryRecordSchema.getName(),
                                "CHECK", entryCheckSchema.getName())))
                .oneOf(List.of(entryRecordSchema, entryCheckSchema)))
        .replaceWithSchema(
            EntryState.class,
            new Schema<EntryState>()
                .oneOf(
                    List.of(
                        converters.readAllAsResolvedSchema(EntryState.Valid.class).schema,
                        converters.readAllAsResolvedSchema(EntryState.Invalid.class).schema)));
  }

  @Bean
  public OpenAPI openapi(@Value("${server.port}") int port, final HateoasProperties properties) {
    return new OpenAPI()
        .info(new Info().title("Spring Electron Test API"))
        .addServersItem(
            new Server().url(String.format("http://%s:%d", properties.domainName(), port)));
  }
}
