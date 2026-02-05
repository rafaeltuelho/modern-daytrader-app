package com.daytrader.trading.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Order Entity - Buy/Sell orders
 * Maps to orders table
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_account_id", columnList = "account_id"),
    @Index(name = "idx_orders_symbol", columnList = "quote_symbol"),
    @Index(name = "idx_orders_status", columnList = "order_status"),
    @Index(name = "idx_orders_open_date", columnList = "open_date")
})
public class Order extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "order_id_seq", allocationSize = 50)
    public Long id;

    @Column(name = "order_type", nullable = false, length = 10)
    public String orderType; // buy, sell

    @Column(name = "order_status", nullable = false, length = 20)
    public String orderStatus; // open, processing, closed, completed, cancelled

    @Column(name = "open_date", nullable = false)
    public Instant openDate;

    @Column(name = "completion_date")
    public Instant completionDate;

    @Column(nullable = false)
    public double quantity;

    @Column(precision = 14, scale = 2)
    public BigDecimal price;

    @Column(name = "order_fee", precision = 14, scale = 2)
    public BigDecimal orderFee;

    @Column(name = "account_id", nullable = false)
    public Long accountId;

    @Column(name = "quote_symbol", nullable = false, length = 10)
    public String quoteSymbol;

    @Column(name = "holding_id")
    public Long holdingId;

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
        if (openDate == null) {
            openDate = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Find orders by account ID
     */
    public static java.util.List<Order> findByAccountId(Long accountId) {
        return list("accountId", accountId);
    }

    /**
     * Find orders by account ID and status
     */
    public static java.util.List<Order> findByAccountIdAndStatus(Long accountId, String status) {
        return list("accountId = ?1 and orderStatus = ?2", accountId, status);
    }

    /**
     * Find orders by account ID and symbol
     */
    public static java.util.List<Order> findByAccountIdAndSymbol(Long accountId, String symbol) {
        return list("accountId = ?1 and quoteSymbol = ?2", accountId, symbol);
    }

    /**
     * Find open orders by account ID
     */
    public static java.util.List<Order> findOpenOrdersByAccountId(Long accountId) {
        return list("accountId = ?1 and orderStatus = 'open'", accountId);
    }
}

