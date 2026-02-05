package com.daytrader.trading.repository;

import com.daytrader.trading.entity.Order;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository for Order entity
 */
@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    public List<Order> findByAccountId(Long accountId) {
        return list("accountId", accountId);
    }

    public List<Order> findByAccountIdAndStatus(Long accountId, String status) {
        return list("accountId = ?1 and orderStatus = ?2", accountId, status);
    }

    public List<Order> findByAccountIdAndSymbol(Long accountId, String symbol) {
        return list("accountId = ?1 and quoteSymbol = ?2", accountId, symbol);
    }

    public List<Order> findOpenOrdersByAccountId(Long accountId) {
        return list("accountId = ?1 and orderStatus = 'open'", accountId);
    }

    public Order findByIdAndAccountId(Long orderId, Long accountId) {
        return find("id = ?1 and accountId = ?2", orderId, accountId).firstResult();
    }
}

