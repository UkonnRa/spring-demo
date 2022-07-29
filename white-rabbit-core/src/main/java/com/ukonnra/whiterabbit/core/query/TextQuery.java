package com.ukonnra.whiterabbit.core.query;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

@JsonSerialize(using = TextQuery.Serializer.class)
@JsonDeserialize(using = TextQuery.Deserializer.class)
public sealed interface TextQuery permits TextQuery.Eq, TextQuery.FullText {

  record Eq(String value) implements TextQuery {}

  record FullText(String value) implements TextQuery {}

  final class Serializer extends StdSerializer<TextQuery> {
    private Serializer() {
      super(TextQuery.class);
    }

    @Override
    public void serialize(TextQuery value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      if (value instanceof Eq eq) {
        gen.writeString(eq.value);
      } else if (value instanceof FullText fullText) {
        gen.writeObject(fullText.value);
      }
    }
  }

  final class Deserializer extends StdDeserializer<TextQuery> {
    private Deserializer() {
      super(TextQuery.class);
    }

    @Override
    public TextQuery deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      try {
        return new Eq(p.readValueAs(String.class));
      } catch (MismatchedInputException ignored) {
      }

      try {
        return new FullText(p.readValueAs(ExternalQuery.FullText.class).value());
      } catch (MismatchedInputException ignored) {
      }

      throw MismatchedInputException.from(p, String.class, "Invalid NameQuery");
    }
  }
}
