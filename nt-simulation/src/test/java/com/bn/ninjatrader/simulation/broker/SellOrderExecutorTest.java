package com.bn.ninjatrader.simulation.broker;

import com.bn.ninjatrader.common.data.Price;
import com.bn.ninjatrader.simulation.account.Account;
import com.bn.ninjatrader.simulation.data.BarData;
import com.bn.ninjatrader.simulation.order.MarketTime;
import com.bn.ninjatrader.simulation.order.Order;
import com.bn.ninjatrader.simulation.transaction.SellTransaction;
import com.bn.ninjatrader.simulation.transaction.Transaction;
import com.bn.ninjatrader.simulation.transaction.TransactionType;
import mockit.Tested;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by Brad on 8/18/16.
 */
public class SellOrderExecutorTest {

  @Tested
  private SellOrderExecutor executor;

  private Account account;

  private final LocalDate now = LocalDate.of(2016, 1, 1);
  private final Price price = Price.builder().date(now).open(1).high(2).low(3).close(4).volume(1000).build();
  private final BarData barData = BarData.builder().price(price).build();
  private final Order order = Order.buy().cashAmount(100000).at(MarketTime.OPEN).build();

  @BeforeMethod
  public void setup() {
    account = Account.withStartingCash(0);
    account.addToPortfolio(Transaction.buy().price(0.5).shares(100000).build());
  }

  @Test
  public void testExecute() {
    final SellTransaction transaction = executor.execute(account, order, barData);

    assertValidTransaction(transaction);
    assertAccountPortfolioSold();
  }

  private void assertValidTransaction(SellTransaction transaction) {
    assertNotNull(transaction);
    assertEquals(transaction.getDate(), now);
    assertEquals(transaction.getNumOfShares(), 100000);
    assertEquals(transaction.getTransactionType(), TransactionType.SELL);
    assertEquals(transaction.getValue(), 100000.0);
  }

  private void assertAccountPortfolioSold() {
    assertEquals(account.getCash(), 100000.0);
    assertEquals(account.getNumOfShares(), 0);
    assertEquals(account.getAvgPrice(), 0.0);
  }
}
