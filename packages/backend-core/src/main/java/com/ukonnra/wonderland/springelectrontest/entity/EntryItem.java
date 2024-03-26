package com.ukonnra.wonderland.springelectrontest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Entity
@Table(name = "entry_items")
public class EntryItem {
  @EmbeddedId @GeneratedValue private EntryItemId id;

  @ManyToOne(optional = false)
  @MapsId("entryId")
  @ToString.Exclude
  private Entry entry;

  @ManyToOne(optional = false)
  @MapsId("accountId")
  @ToString.Exclude
  private Account account;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private BigDecimal price;

  public EntryItem(Entry entry, Account account, BigDecimal amount, BigDecimal price) {
    this.id = new EntryItemId(entry, account);
    this.entry = entry;
    this.account = account;
    this.amount = amount;
    this.price = price;
  }

  @Getter
  @Setter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @AllArgsConstructor
  @Embeddable
  public static class EntryItemId implements Serializable {
    @Column(nullable = false)
    private UUID entryId;

    @Column(nullable = false)
    private UUID accountId;

    public EntryItemId(final Entry entry, final Account account) {
      this.entryId = entry.getId();
      this.accountId = account.getId();
    }
  }
}
