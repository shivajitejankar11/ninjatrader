package com.bn.ninjatrader.thirdparty.exception;

/**
 * Created by Brad on 7/26/16.
 */
public class StockReadFailException extends RuntimeException {

  private final String symbol;

  public StockReadFailException(String symbol, Exception e) {
    super("Failed to read quotes for symbol: " + symbol, e);
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }
}
