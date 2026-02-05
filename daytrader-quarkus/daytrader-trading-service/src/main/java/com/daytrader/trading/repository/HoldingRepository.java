package com.daytrader.trading.repository;

import com.daytrader.trading.entity.Holding;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository for Holding entity
 */
@ApplicationScoped
public class HoldingRepository implements PanacheRepository<Holding> {

    public List<Holding> findByAccountId(Long accountId) {
        return list("accountId", accountId);
    }

    public List<Holding> findByAccountIdAndSymbol(Long accountId, String symbol) {
        return list("accountId = ?1 and quoteSymbol = ?2", accountId, symbol);
    }

    public Holding findByIdAndAccountId(Long holdingId, Long accountId) {
        return find("id = ?1 and accountId = ?2", holdingId, accountId).firstResult();
    }
}

