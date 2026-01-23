/**
 * Business service classes for the DayTrader application.
 *
 * This package contains the CDI-managed service beans that implement
 * business logic for trading operations. All services use @ApplicationScoped
 * and @Transactional annotations where appropriate.
 *
 * Services include:
 * - AuthService: Authentication operations (login, logout, register)
 * - AccountService: Account and profile management operations
 * - TradeService: Core trading operations (buy, sell, orders, holdings)
 * - MarketService: Market summary and quote operations
 */
package com.ibm.websphere.samples.daytrader.services;

