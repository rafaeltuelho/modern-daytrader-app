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
import java.math.RoundingMode;

/**
 * Financial utility methods for DayTrader
 * Migrated from legacy FinancialUtils
 */
public class FinancialUtils {

    public static final RoundingMode ROUND = RoundingMode.HALF_UP;
    public static final int SCALE = 2;
    public static final BigDecimal ZERO = new BigDecimal("0.00").setScale(SCALE, ROUND);
    public static final BigDecimal ONE = new BigDecimal("1.00").setScale(SCALE, ROUND);
    public static final BigDecimal HUNDRED = new BigDecimal("100.00").setScale(SCALE, ROUND);

    public static BigDecimal computeGain(BigDecimal currentBalance, BigDecimal openBalance) {
        return currentBalance.subtract(openBalance).setScale(SCALE, ROUND);
    }

    public static BigDecimal computeGainPercent(BigDecimal currentBalance, BigDecimal openBalance) {
        if (openBalance.doubleValue() == 0.0) {
            return ZERO;
        }
        BigDecimal gainPercent = currentBalance.divide(openBalance, SCALE, ROUND).subtract(ONE).multiply(HUNDRED);
        return gainPercent;
    }
}

