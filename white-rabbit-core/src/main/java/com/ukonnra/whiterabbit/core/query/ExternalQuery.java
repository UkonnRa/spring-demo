package com.ukonnra.whiterabbit.core.query;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;

public sealed interface ExternalQuery permits ExternalQuery.ContainingUser, ExternalQuery.FullText {
  @JsonSerialize(using = ContainingUser.Serializer.class)
  @JsonDeserialize(using = ContainingUser.Deserializer.class)
  record ContainingUser(UUID userId, @Nullable Set<String> fields) implements ExternalQuery {
    public static final String TYPE = "$containingUser";

    static final class Serializer extends StdSerializer<ContainingUser> {
      private Serializer() {
        super(ContainingUser.class);
      }

      @Override
      public void serialize(ContainingUser value, JsonGenerator gen, SerializerProvider provider)
          throws IOException {
        if (value.fields == null) {
          gen.writeObject(Map.of(TYPE, value.userId));
        } else {
          gen.writeObject(Map.of(TYPE, Map.of("userId", value.userId, "fields", value.fields)));
        }
      }
    }

    static final class Deserializer extends StdDeserializer<ContainingUser> {
      private Deserializer() {
        super(ContainingUser.class);
      }

      @Override
      public ContainingUser deserialize(JsonParser p, DeserializationContext ctxt)
          throws IOException {
        final Map<String, Object> obj = p.readValueAs(new TypeReference<Map<String, Object>>() {});
        final var value = obj.get(TYPE);
        if (value instanceof String str) {
          return new ContainingUser(UUID.fromString(str), null);
        } else if (value instanceof Map<?, ?> map
            && map.get("value") instanceof String fieldValue) {
          final var fieldFields =
              Optional.ofNullable(map.get("fields"))
                  .flatMap(
                      v ->
                          Optional.ofNullable(
                              v instanceof Collection<?> collection ? collection : null))
                  .stream()
                  .flatMap(c -> c.stream().map(Object::toString))
                  .collect(Collectors.toSet());
          return new ContainingUser(UUID.fromString(fieldValue), fieldFields);
        }

        throw MismatchedInputException.from(
            p,
            String.class,
            """
          ContainingUser should only be:
          * { $containingUser: <text> }
          * { $containingUser: { userId: <text>, fields: [<field>] } }
          """);
      }
    }
  }

  @JsonSerialize(using = FullText.Serializer.class)
  @JsonDeserialize(using = FullText.Deserializer.class)
  record FullText(String value, @Nullable Set<String> fields) implements ExternalQuery {
    public static final String TYPE = "$fullText";

    static final class Serializer extends StdSerializer<FullText> {
      private Serializer() {
        super(FullText.class);
      }

      @Override
      public void serialize(FullText value, JsonGenerator gen, SerializerProvider provider)
          throws IOException {
        if (value.fields == null) {
          gen.writeObject(Map.of(TYPE, value.value));
        } else {
          gen.writeObject(Map.of(TYPE, Map.of("value", value.value, "fields", value.fields)));
        }
      }
    }

    static final class Deserializer extends StdDeserializer<FullText> {
      private Deserializer() {
        super(FullText.class);
      }

      @Override
      public FullText deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final Map<String, Object> obj = p.readValueAs(new TypeReference<Map<String, Object>>() {});
        final var fullText = obj.get(TYPE);
        if (fullText instanceof String str) {
          return new FullText(str, null);
        } else if (fullText instanceof Map<?, ?> map
            && map.get("value") instanceof String fieldValue) {
          final var fieldFields =
              Optional.ofNullable(map.get("fields"))
                  .flatMap(
                      v ->
                          Optional.ofNullable(
                              v instanceof Collection<?> collection ? collection : null))
                  .stream()
                  .flatMap(c -> c.stream().map(Object::toString))
                  .collect(Collectors.toSet());
          return new FullText(fieldValue, fieldFields);
        }

        throw MismatchedInputException.from(
            p,
            String.class,
            """
          FullText should only be:
          * { $fullText: <text> }
          * { $fullText: { value: <text>, fields: [<field>] } }
          """);
      }
    }
  }
}
