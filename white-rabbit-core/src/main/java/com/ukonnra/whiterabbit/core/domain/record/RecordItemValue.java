package com.ukonnra.whiterabbit.core.domain.record;

import com.ukonnra.whiterabbit.core.domain.account.AccountEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordItemValue {
  @ManyToOne(optional = false)
  private AccountEntity account;

  @Min(0)
  @Column(precision = 10, scale = 2)
  @Nullable
  private BigDecimal amount;

  @Min(0)
  @Column(precision = 10, scale = 2)
  @Nullable
  private BigDecimal price;

  public record Dto(UUID account, @Nullable BigDecimal amount, @Nullable BigDecimal price)
      implements Serializable {
    public static Set<Dto> of(final Collection<RecordItemValue> items) {
      return items.stream()
          .map(item -> new Dto(item.getAccount().getId(), item.getAmount(), item.getPrice()))
          .collect(Collectors.toSet());
    }
  }
}
