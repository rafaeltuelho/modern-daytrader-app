package com.daytrader.trading.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Holding Entity - Stock holdings in portfolio
 * Maps to holding table
 */
@Entity
@Table(name = "holding", indexes = {
    @Index(name = "idx_holding_account_id", columnList = "account_id"),
    @Index(name = "idx_holding_symbol", columnList = "quote_symbol")
})
public class Holding extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "holding_seq")
    @SequenceGenerator(name = "holding_seq", sequenceName = "holding_id_seq", allocationSize = 50)
    public Long id;

    @Column(name = "account_id", nullable = false)
    public Long accountId;

    @Column(name = "quote_symbol", nullable = false, length = 10)
    public String quoteSymbol;

    @Column(nullable = false)
    public double quantity;

    @Column(name = "purchase_price", precision = 14, scale = 2, nullable = false)
    public BigDecimal purchasePrice;

    @Column(name = "purchase_date", nullable = false)
    public Instant purchaseDate;

    @Version
    public int version;

    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (purchaseDate == null) {
            purchaseDate = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Find holdings by account ID
     */
    public static java.util.List<Holding> findByAccountId(Long accountId) {
        return list("accountId", accountId);
    }

    /**
     * Find holdings by account ID and symbol
     */
    public static java.util.List<Holding> findByAccountIdAndSymbol(Long accountId, String symbol) {
        return list("accountId = ?1 and quoteSymbol = ?2", accountId, symbol);
    }

    /**
     * Find holding by ID and account ID (for ownership verification)
     */
    public static Holding findByIdAndAccountId(Long holdingId, Long accountId) {
        return find("id = ?1 and accountId = ?2", holdingId, accountId).firstResult();
    }
}

