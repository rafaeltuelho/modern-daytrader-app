package com.daytrader.market.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Quote Entity - Stock quote information
 * Maps to quote table
 */
@Entity
@Table(name = "quote", indexes = {
    @Index(name = "idx_quote_symbol", columnList = "symbol", unique = true)
})
public class Quote extends PanacheEntityBase {

    @Id
    @Column(length = 10)
    public String symbol;

    @Column(name = "company_name", length = 255)
    public String companyName;

    @Column(nullable = false)
    public double volume;

    @Column(precision = 14, scale = 2, nullable = false)
    public BigDecimal price;

    @Column(name = "open_price", precision = 14, scale = 2)
    public BigDecimal openPrice;

    @Column(name = "low_price", precision = 14, scale = 2)
    public BigDecimal lowPrice;

    @Column(name = "high_price", precision = 14, scale = 2)
    public BigDecimal highPrice;

    @Column(name = "price_change")
    public double priceChange;

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
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Find quote by symbol
     */
    public static Quote findBySymbol(String symbol) {
        return findById(symbol.toUpperCase());
    }

    /**
     * Check if quote exists for symbol
     */
    public static boolean existsBySymbol(String symbol) {
        return findById(symbol.toUpperCase()) != null;
    }

    /**
     * Find all quotes ordered by symbol
     */
    public static java.util.List<Quote> findAllOrdered() {
        return listAll(io.quarkus.panache.common.Sort.by("symbol"));
    }
}

