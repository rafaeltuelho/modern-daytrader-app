package com.daytrader.trading.service;

import com.daytrader.common.dto.OrderDTO;
import com.daytrader.common.exception.BusinessException;
import com.daytrader.common.exception.ResourceNotFoundException;
import com.daytrader.trading.entity.Order;
import com.daytrader.trading.repository.OrderRepository;
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
 * Unit tests for OrderService
 */
@QuarkusTest
class OrderServiceTest {

    @Inject
    OrderService orderService;

    @Inject
    OrderRepository orderRepository;

    private Long testAccountId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();
        
        // Create test account ID
        testAccountId = 1000L + System.currentTimeMillis() % 1000;
    }

    @AfterEach
    @Transactional
    void tearDown() {
        cleanupTestData();
    }

    @Transactional
    void cleanupTestData() {
        // Delete all test orders
        orderRepository.deleteAll();
    }

    @Test
    void testCreateOrder_Buy() {
        // Create a buy order
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType("buy");
        orderDTO.setAccountId(testAccountId);
        orderDTO.setSymbol("IBM");
        orderDTO.setQuantity(100.0);
        orderDTO.setPrice(new BigDecimal("150.00"));

        OrderDTO created = orderService.createOrder(orderDTO);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("buy", created.getOrderType());
        assertEquals("open", created.getOrderStatus());
        assertEquals(testAccountId, created.getAccountId());
        assertEquals("IBM", created.getSymbol());
        assertEquals(100.0, created.getQuantity());
        assertEquals(new BigDecimal("150.00"), created.getPrice());
        assertEquals(new BigDecimal("9.95"), created.getOrderFee()); // Default fee
        assertNotNull(created.getOpenDate());
    }

    @Test
    void testCreateOrder_Sell() {
        // Create a sell order
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType("sell");
        orderDTO.setAccountId(testAccountId);
        orderDTO.setSymbol("AAPL");
        orderDTO.setQuantity(50.0);
        orderDTO.setPrice(new BigDecimal("175.50"));
        orderDTO.setOrderFee(new BigDecimal("5.00")); // Custom fee

        OrderDTO created = orderService.createOrder(orderDTO);

        assertNotNull(created);
        assertEquals("sell", created.getOrderType());
        assertEquals("open", created.getOrderStatus());
        assertEquals(new BigDecimal("5.00"), created.getOrderFee()); // Custom fee preserved
    }

    @Test
    void testGetOrder() {
        // Create an order first
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType("buy");
        orderDTO.setAccountId(testAccountId);
        orderDTO.setSymbol("MSFT");
        orderDTO.setQuantity(75.0);
        orderDTO.setPrice(new BigDecimal("300.00"));

        OrderDTO created = orderService.createOrder(orderDTO);

        // Get the order
        OrderDTO retrieved = orderService.getOrder(created.getId(), testAccountId);

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals("MSFT", retrieved.getSymbol());
    }

    @Test
    void testGetOrder_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrder(999999L, testAccountId);
        });
    }

    @Test
    void testListOrders() {
        // Create multiple orders
        for (int i = 0; i < 3; i++) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderType("buy");
            orderDTO.setAccountId(testAccountId);
            orderDTO.setSymbol("STOCK" + i);
            orderDTO.setQuantity(100.0);
            orderDTO.setPrice(new BigDecimal("100.00"));
            orderService.createOrder(orderDTO);
        }

        List<OrderDTO> orders = orderService.listOrders(testAccountId);

        assertNotNull(orders);
        assertEquals(3, orders.size());
    }

    @Test
    void testListOrdersByStatus() {
        // Create orders with different statuses
        OrderDTO order1 = new OrderDTO();
        order1.setOrderType("buy");
        order1.setAccountId(testAccountId);
        order1.setSymbol("IBM");
        order1.setQuantity(100.0);
        order1.setPrice(new BigDecimal("150.00"));
        OrderDTO created = orderService.createOrder(order1);

        // All orders should be "open" initially
        List<OrderDTO> openOrders = orderService.listOrdersByStatus(testAccountId, "open");
        assertNotNull(openOrders);
        assertEquals(1, openOrders.size());
        assertEquals("open", openOrders.get(0).getOrderStatus());
    }

    @Test
    void testCancelOrder() {
        // Create an order
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType("buy");
        orderDTO.setAccountId(testAccountId);
        orderDTO.setSymbol("GOOGL");
        orderDTO.setQuantity(25.0);
        orderDTO.setPrice(new BigDecimal("2800.00"));

        OrderDTO created = orderService.createOrder(orderDTO);
        assertEquals("open", created.getOrderStatus());

        // Cancel the order
        OrderDTO cancelled = orderService.cancelOrder(created.getId(), testAccountId);

        assertNotNull(cancelled);
        assertEquals("cancelled", cancelled.getOrderStatus());
        assertNotNull(cancelled.getCompletionDate());
    }

    @Test
    void testCancelOrder_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.cancelOrder(999999L, testAccountId);
        });
    }

    @Test
    void testCancelOrder_AlreadyCancelled() {
        // Create and cancel an order
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType("buy");
        orderDTO.setAccountId(testAccountId);
        orderDTO.setSymbol("TSLA");
        orderDTO.setQuantity(10.0);
        orderDTO.setPrice(new BigDecimal("700.00"));

        OrderDTO created = orderService.createOrder(orderDTO);
        orderService.cancelOrder(created.getId(), testAccountId);

        // Try to cancel again
        assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(created.getId(), testAccountId);
        });
    }

    @Test
    void testCompleteOrder() {
        // Create an order
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType("buy");
        orderDTO.setAccountId(testAccountId);
        orderDTO.setSymbol("NFLX");
        orderDTO.setQuantity(15.0);
        orderDTO.setPrice(new BigDecimal("500.00"));

        OrderDTO created = orderService.createOrder(orderDTO);

        // Complete the order
        OrderDTO completed = orderService.completeOrder(created.getId());

        assertNotNull(completed);
        assertEquals("completed", completed.getOrderStatus());
        assertNotNull(completed.getCompletionDate());
    }

    @Test
    void testCompleteOrder_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.completeOrder(999999L);
        });
    }
}

