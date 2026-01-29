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
package com.ibm.websphere.samples.daytrader.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Market Summary data
 * Per Phase 3: Backend Migration specification section 4.2
 */
public class MarketSummaryDTO {

    private BigDecimal TSIA;
    private BigDecimal openTSIA;
    private double volume;
    private List<QuoteDTO> topGainers;
    private List<QuoteDTO> topLosers;
    private long summaryDate;

    public MarketSummaryDTO() {
    }

    public MarketSummaryDTO(BigDecimal TSIA, BigDecimal openTSIA, double volume, 
                           List<QuoteDTO> topGainers, List<QuoteDTO> topLosers) {
        this.TSIA = TSIA;
        this.openTSIA = openTSIA;
        this.volume = volume;
        this.topGainers = topGainers;
        this.topLosers = topLosers;
        this.summaryDate = System.currentTimeMillis();
    }

    // Getters and Setters
    public BigDecimal getTSIA() {
        return TSIA;
    }

    public void setTSIA(BigDecimal TSIA) {
        this.TSIA = TSIA;
    }

    public BigDecimal getOpenTSIA() {
        return openTSIA;
    }

    public void setOpenTSIA(BigDecimal openTSIA) {
        this.openTSIA = openTSIA;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public List<QuoteDTO> getTopGainers() {
        return topGainers;
    }

    public void setTopGainers(List<QuoteDTO> topGainers) {
        this.topGainers = topGainers;
    }

    public List<QuoteDTO> getTopLosers() {
        return topLosers;
    }

    public void setTopLosers(List<QuoteDTO> topLosers) {
        this.topLosers = topLosers;
    }

    public long getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(long summaryDate) {
        this.summaryDate = summaryDate;
    }
}

