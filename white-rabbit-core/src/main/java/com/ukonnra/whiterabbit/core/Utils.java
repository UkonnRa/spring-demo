package com.ukonnra.whiterabbit.core;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public interface Utils {
  static UUID decodeCursor(final String cursor) {
    return UUID.fromString(new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8));
  }

  static String encodeCursor(final UUID id) {
    return Base64.getUrlEncoder().encodeToString(id.toString().getBytes(StandardCharsets.UTF_8));
  }
}
