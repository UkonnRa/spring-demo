package com.ukonnra.whiterabbit.core.domain.user;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthIdValue implements Serializable {
  @NotBlank
  @Column(nullable = false)
  private String provider;

  @NotBlank
  @Column(nullable = false)
  private String tokenValue;
}
