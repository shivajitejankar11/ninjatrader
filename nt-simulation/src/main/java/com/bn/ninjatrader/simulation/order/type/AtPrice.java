package com.bn.ninjatrader.simulation.order.type;

import com.bn.ninjatrader.common.model.Price;
import com.bn.ninjatrader.simulation.data.BarData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bradwee2000@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtPrice implements OrderType {
  private static final Logger LOG = LoggerFactory.getLogger(AtPrice.class);

  public static final AtPrice of(final double price) {
    return new AtPrice(price);
  }

  @JsonProperty("price")
  private final double price;

  public AtPrice(@JsonProperty("price") final double price) {
    this.price = price;
  }

  @Override
  public boolean isFulfillable(final BarData onSubmitBarData, final BarData currentBarData) {
    final Price currentPrice = currentBarData.getPrice();
    return price >= currentPrice.getLow() && price <= currentPrice.getHigh();
  }

  @Override
  public double getFulfilledPrice(final BarData currentBar) {
    return price;
  }


  @Override
  public boolean equals(final Object obj) {
    if (obj == null || !(obj instanceof AtPrice)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    final AtPrice rhs = (AtPrice) obj;
    return Objects.equal(price, rhs.price);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(price);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("price", price).toString();
  }
}
