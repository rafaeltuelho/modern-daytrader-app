package com.daytrader.trading.service;

import com.daytrader.common.dto.OrderDTO;
import com.daytrader.common.event.OrderCompletedEvent;
import com.daytrader.common.event.OrderCreatedEvent;
import com.daytrader.common.exception.BusinessException;
import com.daytrader.common.exception.ResourceNotFoundException;
import com.daytrader.trading.entity.Order;
import com.daytrader.trading.mapper.TradingMapper;
import com.daytrader.trading.messaging.OrderEventProducer;
import com.daytrader.trading.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Order operations
 */
@ApplicationScoped
public class OrderService {

    @Inject
    OrderRepository orderRepository;

    @Inject
    TradingMapper mapper;

    @Inject
    OrderEventProducer orderEventProducer;

    /**
     * Create a new order
     */
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = mapper.toOrder(orderDTO);
        order.orderStatus = "open";
        order.openDate = Instant.now();

        // Set default order fee if not provided
        if (order.orderFee == null) {
            order.orderFee = new BigDecimal("9.95");
        }

        orderRepository.persist(order);

        // Emit OrderCreatedEvent to Kafka
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.id,
            order.orderType,
            order.accountId,
            order.quoteSymbol,
            order.quantity,
            order.price,
            Instant.now()
        );
        orderEventProducer.emitOrderCreated(event);

        return mapper.toOrderDTO(order);
    }

    /**
     * Get order by ID
     */
    public OrderDTO getOrder(Long orderId, Long accountId) {
        Order order = orderRepository.findByIdAndAccountId(orderId, accountId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
        return mapper.toOrderDTO(order);
    }

    /**
     * List orders by account ID
     */
    public List<OrderDTO> listOrders(Long accountId) {
        return orderRepository.findByAccountId(accountId)
                .stream()
                .map(mapper::toOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * List orders by account ID and status
     */
    public List<OrderDTO> listOrdersByStatus(Long accountId, String status) {
        return orderRepository.findByAccountIdAndStatus(accountId, status)
                .stream()
                .map(mapper::toOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cancel an order
     */
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long accountId) {
        Order order = orderRepository.findByIdAndAccountId(orderId, accountId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
        
        if (!"open".equals(order.orderStatus)) {
            throw new BusinessException("Cannot cancel order in status: " + order.orderStatus);
        }
        
        order.orderStatus = "cancelled";
        order.completionDate = Instant.now();
        orderRepository.persist(order);
        
        return mapper.toOrderDTO(order);
    }

    /**
     * Complete an order
     */
    @Transactional
    public OrderDTO completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }

        order.orderStatus = "completed";
        order.completionDate = Instant.now();
        orderRepository.persist(order);

        // Emit OrderCompletedEvent to Kafka
        OrderCompletedEvent event = new OrderCompletedEvent(
            order.id,
            order.orderType,
            order.orderStatus,
            order.accountId,
            order.quoteSymbol,
            order.quantity,
            order.price,
            order.orderFee,
            order.completionDate,
            Instant.now()
        );
        orderEventProducer.emitOrderCompleted(event);

        return mapper.toOrderDTO(order);
    }
}

