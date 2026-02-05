package com.daytrader.trading.mapper;

import com.daytrader.common.dto.HoldingDTO;
import com.daytrader.common.dto.OrderDTO;
import com.daytrader.trading.entity.Holding;
import com.daytrader.trading.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for Trading entities to DTOs
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface TradingMapper {

    @Mapping(source = "quoteSymbol", target = "symbol")
    OrderDTO toOrderDTO(Order order);

    @Mapping(source = "symbol", target = "quoteSymbol")
    @Mapping(target = "version", ignore = true)
    Order toOrder(OrderDTO orderDTO);

    @Mapping(source = "quoteSymbol", target = "symbol")
    HoldingDTO toHoldingDTO(Holding holding);

    @Mapping(source = "symbol", target = "quoteSymbol")
    @Mapping(target = "version", ignore = true)
    Holding toHolding(HoldingDTO holdingDTO);
}

