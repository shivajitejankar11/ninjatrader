package com.bn.ninjatrader.simulation.order.type;

import com.bn.ninjatrader.simulation.data.BarData;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by Brad on 8/13/16.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MarketOpen.class, name = "open"),
    @JsonSubTypes.Type(value = MarketClose.class, name = "close"),
    @JsonSubTypes.Type(value = AtPrice.class, name = "atPrice")
})
public interface OrderType {

  boolean isFulfillable(final BarData onSubmitBarData, final BarData currentBarData);

  double getFulfilledPrice(final BarData currentBar);
}
