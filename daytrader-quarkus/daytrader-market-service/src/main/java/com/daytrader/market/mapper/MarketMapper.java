package com.daytrader.market.mapper;

import com.daytrader.common.dto.QuoteDTO;
import com.daytrader.market.entity.Quote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for Market entities to DTOs
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface MarketMapper {

    QuoteDTO toQuoteDTO(Quote quote);

    @Mapping(target = "version", ignore = true)
    Quote toQuote(QuoteDTO quoteDTO);
}

