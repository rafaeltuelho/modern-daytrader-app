package com.daytrader.trading.service;

import com.daytrader.common.dto.HoldingDTO;
import com.daytrader.common.exception.ResourceNotFoundException;
import com.daytrader.trading.entity.Holding;
import com.daytrader.trading.mapper.TradingMapper;
import com.daytrader.trading.repository.HoldingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Holding operations
 */
@ApplicationScoped
public class HoldingService {

    @Inject
    HoldingRepository holdingRepository;

    @Inject
    TradingMapper mapper;

    /**
     * Create a new holding
     */
    @Transactional
    public HoldingDTO createHolding(HoldingDTO holdingDTO) {
        Holding holding = mapper.toHolding(holdingDTO);
        holdingRepository.persist(holding);
        return mapper.toHoldingDTO(holding);
    }

    /**
     * Get holding by ID
     */
    public HoldingDTO getHolding(Long holdingId, Long accountId) {
        Holding holding = holdingRepository.findByIdAndAccountId(holdingId, accountId);
        if (holding == null) {
            throw new ResourceNotFoundException("Holding not found: " + holdingId);
        }
        return mapper.toHoldingDTO(holding);
    }

    /**
     * List holdings by account ID
     */
    public List<HoldingDTO> listHoldings(Long accountId) {
        return holdingRepository.findByAccountId(accountId)
                .stream()
                .map(mapper::toHoldingDTO)
                .collect(Collectors.toList());
    }

    /**
     * List holdings by account ID and symbol
     */
    public List<HoldingDTO> listHoldingsBySymbol(Long accountId, String symbol) {
        return holdingRepository.findByAccountIdAndSymbol(accountId, symbol)
                .stream()
                .map(mapper::toHoldingDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete a holding
     */
    @Transactional
    public void deleteHolding(Long holdingId, Long accountId) {
        Holding holding = holdingRepository.findByIdAndAccountId(holdingId, accountId);
        if (holding == null) {
            throw new ResourceNotFoundException("Holding not found: " + holdingId);
        }
        holdingRepository.delete(holding);
    }
}

