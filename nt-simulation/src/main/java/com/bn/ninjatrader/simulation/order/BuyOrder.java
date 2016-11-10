package com.bn.ninjatrader.simulation.order;

import com.bn.ninjatrader.simulation.transaction.TransactionType;

import java.time.LocalDate;

/**
 * Created by Brad on 8/12/16.
 */
public class BuyOrder extends Order {

  private double cashAmount;

  private BuyOrder(LocalDate orderDate,
                   MarketTime marketTime,
                   int barsFromNow,
                   long numOfShares,
                   double cashAmount) {
    super(orderDate, TransactionType.BUY, marketTime, barsFromNow, numOfShares);
    this.cashAmount = cashAmount;
  }

  public double getCashAmount() {
    return cashAmount;
  }

  public static class BuyOrderBuilder extends OrderBuilder<BuyOrderBuilder> {
    private double cashAmount;

    public BuyOrderBuilder cashAmount(double cashAmount) {
      this.cashAmount = cashAmount;
      return this;
    }

    public BuyOrderBuilder params(OrderParameters orderParams) {
      return getThis()
          .at(orderParams.getMarketTime())
          .barsFromNow(orderParams.getBarsFromNow());
    }

    @Override
    BuyOrderBuilder getThis() {
      return this;
    }

    @Override
    public BuyOrder build() {
      BuyOrder order = new BuyOrder(getOrderDate(), getMarketTime(), getDaysFromNow(), getNumOfShares(), cashAmount);
      return order;
    }
  }
}