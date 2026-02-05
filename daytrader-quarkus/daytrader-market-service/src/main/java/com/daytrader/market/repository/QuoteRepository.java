package com.daytrader.market.repository;

import com.daytrader.market.entity.Quote;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository for Quote entity
 */
@ApplicationScoped
public class QuoteRepository implements PanacheRepositoryBase<Quote, String> {

    public Quote findBySymbol(String symbol) {
        return findById(symbol.toUpperCase());
    }

    public boolean existsBySymbol(String symbol) {
        return findById(symbol.toUpperCase()) != null;
    }

    public List<Quote> findAllOrdered() {
        return listAll(Sort.by("symbol"));
    }

    public List<Quote> findTopGainers(int limit) {
        return find("ORDER BY priceChange DESC").page(0, limit).list();
    }

    public List<Quote> findTopLosers(int limit) {
        return find("ORDER BY priceChange ASC").page(0, limit).list();
    }
}

