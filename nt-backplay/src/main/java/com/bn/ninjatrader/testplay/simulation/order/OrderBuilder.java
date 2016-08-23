package com.bn.ninjatrader.testplay.simulation.order;

import com.bn.ninjatrader.testplay.simulation.account.Account;

import java.time.LocalDate;

/**
 * Created by Brad on 8/13/16.
 */
abstract class OrderBuilder<T extends OrderBuilder> {
  private Account account;
  private long numOfShares;
  private int daysFromNow = 0;
  private LocalDate orderDate;
  private MarketTime marketTime = MarketTime.CLOSE;

  public T account(Account account) {
    this.account = account;
    return getThis();
  }

  public T date(LocalDate orderDate) {
    this.orderDate = orderDate;
    return getThis();
  }

  public T shares(long numOfShares) {
    this.numOfShares = numOfShares;
    return getThis();
  }

  public T daysFromNow(int daysFromNow) {
    this.daysFromNow = daysFromNow;
    return getThis();
  }

  public T at(MarketTime marketTime) {
    this.marketTime = marketTime;
    return getThis();
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public long getNumOfShares() {
    return numOfShares;
  }

  public int getDaysFromNow() {
    return daysFromNow;
  }

  public MarketTime getMarketTime() {
    return marketTime;
  }

  abstract T getThis();

  public abstract Order build();
}