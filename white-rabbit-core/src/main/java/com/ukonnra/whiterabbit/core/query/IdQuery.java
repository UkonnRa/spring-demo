package com.ukonnra.whiterabbit.core.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@JsonSerialize(using = IdQuery.Serializer.class)
@JsonDeserialize(using = IdQuery.Deserializer.class)
public sealed interface IdQuery permits IdQuery.Single, IdQuery.Multiple {
  static Optional<IdQuery> of(UUID... id) {
    return of(Arrays.stream(id).toList());
  }

  static Optional<IdQuery> of(Collection<UUID> id) {
    if (id.isEmpty()) {
      return Optional.empty();
    } else if (id.size() == 1) {
      return Optional.of(new Single(id.iterator().next()));
    } else {
      return Optional.of(new Multiple(new HashSet<>(id)));
    }
  }

  @JsonIgnore
  Set<UUID> idSet();

  record Single(UUID id) implements IdQuery {
    @Override
    public Set<UUID> idSet() {
      return Set.of(id);
    }
  }

  record Multiple(Set<UUID> ids) implements IdQuery {
    @Override
    public Set<UUID> idSet() {
      return ids;
    }
  }

  final class Serializer extends StdSerializer<IdQuery> {
    private Serializer() {
      super(IdQuery.class);
    }

    @Override
    public void serialize(IdQuery value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      if (value instanceof IdQuery.Single single) {
        gen.writeString(single.id.toString());
      } else if (value instanceof IdQuery.Multiple multiple) {
        gen.writeStartArray();
        for (final var id : multiple.ids) {
          gen.writeString(id.toString());
        }
        gen.writeEndArray();
      }
    }
  }

  final class Deserializer extends StdDeserializer<IdQuery> {
    private Deserializer() {
      super(IdQuery.class);
    }

    @Override
    public IdQuery deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      try {
        return new Single(p.readValueAs(UUID.class));
      } catch (MismatchedInputException ignored) {
      }

      try {
        return new Multiple(p.readValueAs(new TypeReference<List<UUID>>() {}));
      } catch (MismatchedInputException ignored) {
      }

      throw MismatchedInputException.from(
          p, String.class, "IdQuery should only be String or Array");
    }
  }
}
