package com.daytrader.trading.service;

import com.daytrader.common.dto.HoldingDTO;
import com.daytrader.common.dto.OrderDTO;
import com.daytrader.common.dto.PortfolioSummaryResponse;
import com.daytrader.trading.entity.Holding;
import com.daytrader.trading.entity.Order;
import com.daytrader.trading.mapper.TradingMapper;
import com.daytrader.trading.repository.HoldingRepository;
import com.daytrader.trading.repository.OrderRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Portfolio operations
 * Implements portfolio summary per api-spec-trading.md
 */
@ApplicationScoped
public class PortfolioService {

    private static final Logger LOG = Logger.getLogger(PortfolioService.class);

    @Inject
    HoldingRepository holdingRepository;

    @Inject
    OrderRepository orderRepository;

    @Inject
    TradingMapper mapper;

    /**
     * Get portfolio summary for an account
     * Per api-spec-trading.md specification
     */
    public PortfolioSummaryResponse getPortfolioSummary(Long accountId, BigDecimal cashBalance) {
        LOG.debugf("Getting portfolio summary for account: %d", accountId);

        // Get all holdings for the account
        List<Holding> holdings = holdingRepository.findByAccountId(accountId);

        // Calculate holdings value and total gain
        BigDecimal holdingsValue = BigDecimal.ZERO;
        BigDecimal totalPurchaseValue = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            BigDecimal purchaseValue = holding.purchasePrice.multiply(BigDecimal.valueOf(holding.quantity));
            totalPurchaseValue = totalPurchaseValue.add(purchaseValue);
            
            // For now, use purchase price as current price (in real scenario, would fetch current quote)
            // TODO: Integrate with Market Service to get current prices
            BigDecimal currentValue = holding.purchasePrice.multiply(BigDecimal.valueOf(holding.quantity));
            holdingsValue = holdingsValue.add(currentValue);
        }

        // Calculate total gain/loss
        BigDecimal totalGain = holdingsValue.subtract(totalPurchaseValue);

        // Calculate total gain percentage
        Double totalGainPercent = 0.0;
        if (totalPurchaseValue.compareTo(BigDecimal.ZERO) > 0) {
            totalGainPercent = totalGain
                .divide(totalPurchaseValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }

        // Calculate total portfolio value (cash + holdings)
        BigDecimal totalValue = cashBalance.add(holdingsValue);

        // Get recent orders (last 5)
        List<OrderDTO> recentOrders = orderRepository
            .find("accountId = ?1 ORDER BY openDate DESC", accountId)
            .page(0, 5)
            .list()
            .stream()
            .map(mapper::toOrderDTO)
            .collect(Collectors.toList());

        // Get top holdings (by value, top 5)
        List<HoldingDTO> topHoldings = holdings.stream()
            .sorted((h1, h2) -> {
                BigDecimal value1 = h1.purchasePrice.multiply(BigDecimal.valueOf(h1.quantity));
                BigDecimal value2 = h2.purchasePrice.multiply(BigDecimal.valueOf(h2.quantity));
                return value2.compareTo(value1); // Descending order
            })
            .limit(5)
            .map(mapper::toHoldingDTO)
            .collect(Collectors.toList());

        return new PortfolioSummaryResponse(
            accountId,
            cashBalance,
            holdingsValue,
            totalValue,
            totalGain,
            totalGainPercent,
            holdings.size(),
            recentOrders,
            topHoldings
        );
    }
}

