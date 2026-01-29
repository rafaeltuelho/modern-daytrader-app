/**
 * (C) Copyright IBM Corporation 2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.repository;

import java.util.List;
import java.util.Optional;

import com.ibm.websphere.samples.daytrader.entity.Order;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Repository for Order entity using Panache Repository pattern
 * Per Phase 3: Backend Migration specification section 3.2
 */
@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    @Inject
    EntityManager entityManager;

    /**
     * Find orders by account ID
     */
    public List<Order> findByAccountId(Integer accountId) {
        return find("account.accountID = ?1 ORDER BY openDate DESC", accountId).list();
    }

    /**
     * Find orders by account ID and status
     */
    public List<Order> findByAccountIdAndStatus(Integer accountId, String status) {
        return find("account.accountID = ?1 and orderStatus = ?2 ORDER BY openDate DESC", accountId, status).list();
    }

    /**
     * Find closed orders by user ID
     */
    public List<Order> findClosedOrdersByUserId(String userId) {
        return find("orderStatus = 'closed' and account.profile.userID = ?1", userId).list();
    }

    /**
     * Find order by ID with account, quote, and holding eagerly loaded
     */
    public Optional<Order> findByIdWithDetails(Integer orderId) {
        return find("SELECT o FROM Order o LEFT JOIN FETCH o.account LEFT JOIN FETCH o.quote LEFT JOIN FETCH o.holding WHERE o.orderID = ?1", orderId)
                .firstResultOptional();
    }

    /**
     * Find orders by status
     */
    public List<Order> findByStatus(String status) {
        return find("orderStatus", status).list();
    }

    /**
     * Complete closed orders for a user
     */
    @Transactional
    public int completeClosedOrders(String userId) {
        return update("orderStatus = 'completed' WHERE orderStatus = 'closed' AND account.profile.userID = ?1", userId);
    }

    /**
     * Find recent orders by account ID
     */
    public List<Order> findRecentByAccountId(Integer accountId, int limit) {
        return find("account.accountID = ?1 ORDER BY openDate DESC", accountId).page(0, limit).list();
    }

    /**
     * Clear holding reference from all orders that reference the given holding.
     * This is needed before deleting a holding to avoid FK constraint violations.
     */
    @Transactional
    public int clearHoldingReference(Integer holdingId) {
        return update("holding = null WHERE holding.holdingID = ?1", holdingId);
    }

    /**
     * Set holding reference for an order using a native SQL update.
     * This avoids transient entity issues when the holding was created in a different transaction.
     */
    @Transactional
    public int setHoldingReference(Integer orderId, Integer holdingId) {
        return entityManager.createNativeQuery(
            "UPDATE ORDEREJB SET HOLDING_HOLDINGID = ?1 WHERE ORDERID = ?2")
            .setParameter(1, holdingId)
            .setParameter(2, orderId)
            .executeUpdate();
    }
}

