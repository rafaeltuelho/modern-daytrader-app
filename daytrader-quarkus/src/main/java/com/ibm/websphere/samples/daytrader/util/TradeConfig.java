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
package com.ibm.websphere.samples.daytrader.util;

import java.math.BigDecimal;

/**
 * Trade configuration constants
 * Migrated from legacy TradeConfig
 */
public class TradeConfig {

    // Order processing modes
    public static final int SYNCH = 0;
    public static final int ASYNCH_2PHASE = 1;
    public static final int ASYNCH_MANAGEDTHREAD = 2;

    // Default order fee
    private static final BigDecimal ORDER_FEE = new BigDecimal("24.95");
    private static final BigDecimal CASH_FEE = new BigDecimal("0.00");

    // Stock price limits
    public static final BigDecimal PENNY_STOCK_PRICE = new BigDecimal("0.01");
    public static final BigDecimal PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER = new BigDecimal("600.0");
    public static final BigDecimal MAXIMUM_STOCK_PRICE = new BigDecimal("400.00");
    public static final BigDecimal MAXIMUM_STOCK_SPLIT_MULTIPLIER = new BigDecimal("0.5");

    /**
     * Get order fee based on order type
     */
    public static BigDecimal getOrderFee(String orderType) {
        if ("buy".equalsIgnoreCase(orderType) || "sell".equalsIgnoreCase(orderType)) {
            return ORDER_FEE;
        }
        return CASH_FEE;
    }
}

