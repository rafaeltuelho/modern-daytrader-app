package com.daytrader.trading.service;

import com.daytrader.common.dto.HoldingDTO;
import com.daytrader.common.exception.ResourceNotFoundException;
import com.daytrader.trading.entity.Holding;
import com.daytrader.trading.repository.HoldingRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HoldingService
 */
@QuarkusTest
class HoldingServiceTest {

    @Inject
    HoldingService holdingService;

    @Inject
    HoldingRepository holdingRepository;

    private Long testAccountId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();
        
        // Create test account ID
        testAccountId = 2000L + System.currentTimeMillis() % 1000;
    }

    @AfterEach
    @Transactional
    void tearDown() {
        cleanupTestData();
    }

    @Transactional
    void cleanupTestData() {
        // Delete all test holdings
        holdingRepository.deleteAll();
    }

    @Test
    void testCreateHolding() {
        HoldingDTO holdingDTO = new HoldingDTO();
        holdingDTO.setAccountId(testAccountId);
        holdingDTO.setSymbol("IBM");
        holdingDTO.setQuantity(100.0);
        holdingDTO.setPurchasePrice(new BigDecimal("150.00"));
        holdingDTO.setPurchaseDate(Instant.now());

        HoldingDTO created = holdingService.createHolding(holdingDTO);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(testAccountId, created.getAccountId());
        assertEquals("IBM", created.getSymbol());
        assertEquals(100.0, created.getQuantity());
        assertEquals(new BigDecimal("150.00"), created.getPurchasePrice());
        assertNotNull(created.getPurchaseDate());
    }

    @Test
    void testGetHolding() {
        // Create a holding first
        HoldingDTO holdingDTO = new HoldingDTO();
        holdingDTO.setAccountId(testAccountId);
        holdingDTO.setSymbol("AAPL");
        holdingDTO.setQuantity(50.0);
        holdingDTO.setPurchasePrice(new BigDecimal("175.50"));
        holdingDTO.setPurchaseDate(Instant.now());

        HoldingDTO created = holdingService.createHolding(holdingDTO);

        // Get the holding
        HoldingDTO retrieved = holdingService.getHolding(created.getId(), testAccountId);

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals("AAPL", retrieved.getSymbol());
        assertEquals(50.0, retrieved.getQuantity());
    }

    @Test
    void testGetHolding_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            holdingService.getHolding(999999L, testAccountId);
        });
    }

    @Test
    void testListHoldings() {
        // Create multiple holdings
        String[] symbols = {"IBM", "AAPL", "MSFT"};
        for (String symbol : symbols) {
            HoldingDTO holdingDTO = new HoldingDTO();
            holdingDTO.setAccountId(testAccountId);
            holdingDTO.setSymbol(symbol);
            holdingDTO.setQuantity(100.0);
            holdingDTO.setPurchasePrice(new BigDecimal("100.00"));
            holdingDTO.setPurchaseDate(Instant.now());
            holdingService.createHolding(holdingDTO);
        }

        List<HoldingDTO> holdings = holdingService.listHoldings(testAccountId);

        assertNotNull(holdings);
        assertEquals(3, holdings.size());
    }

    @Test
    void testListHoldingsBySymbol() {
        // Create holdings for different symbols
        HoldingDTO holding1 = new HoldingDTO();
        holding1.setAccountId(testAccountId);
        holding1.setSymbol("IBM");
        holding1.setQuantity(100.0);
        holding1.setPurchasePrice(new BigDecimal("150.00"));
        holding1.setPurchaseDate(Instant.now());
        holdingService.createHolding(holding1);

        HoldingDTO holding2 = new HoldingDTO();
        holding2.setAccountId(testAccountId);
        holding2.setSymbol("IBM");
        holding2.setQuantity(50.0);
        holding2.setPurchasePrice(new BigDecimal("155.00"));
        holding2.setPurchaseDate(Instant.now());
        holdingService.createHolding(holding2);

        HoldingDTO holding3 = new HoldingDTO();
        holding3.setAccountId(testAccountId);
        holding3.setSymbol("AAPL");
        holding3.setQuantity(75.0);
        holding3.setPurchasePrice(new BigDecimal("175.00"));
        holding3.setPurchaseDate(Instant.now());
        holdingService.createHolding(holding3);

        // List holdings for IBM only
        List<HoldingDTO> ibmHoldings = holdingService.listHoldingsBySymbol(testAccountId, "IBM");

        assertNotNull(ibmHoldings);
        assertEquals(2, ibmHoldings.size());
        assertTrue(ibmHoldings.stream().allMatch(h -> "IBM".equals(h.getSymbol())));
    }
}

