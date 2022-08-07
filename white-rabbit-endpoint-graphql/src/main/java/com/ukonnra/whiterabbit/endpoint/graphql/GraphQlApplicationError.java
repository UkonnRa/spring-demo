package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.CoreError;
import lombok.Getter;

public abstract class GraphQlApplicationError extends CoreError {
  protected GraphQlApplicationError(String message) {
    super(message);
  }

  @Getter
  public static class FirstLastBothExist extends GraphQlApplicationError {

    public FirstLastBothExist() {
      super("Field[first] and Field[last] should not both exist in the pagination query");
    }

    @Override
    public String getType() {
      return "FirstLastBothExist";
    }
  }
}
