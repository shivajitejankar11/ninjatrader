package com.bn.ninjatrader.simulation.order;

import com.bn.ninjatrader.simulation.order.type.OrderType;
import com.bn.ninjatrader.simulation.transaction.TransactionType;

import java.time.LocalDate;

/**
 * Created by Brad on 8/12/16.
 */
public class SellOrder extends AbstractOrder {

  public static final Builder builder() {
    return new Builder();
  }

  private SellOrder(final LocalDate orderDate,
                    final OrderType orderType,
                    final OrderConfig orderConfig,
                    final long numOfShares) {
    super(orderDate, TransactionType.SELL, orderType, orderConfig, numOfShares);
  }

  /**
   * Builder class.
   */
  public static class Builder extends OrderBuilder<Builder> {
    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public SellOrder build() {
      return new SellOrder(getOrderDate(), getOrderType(), getOrderConfig(), getNumOfShares());
    }
  }
}
