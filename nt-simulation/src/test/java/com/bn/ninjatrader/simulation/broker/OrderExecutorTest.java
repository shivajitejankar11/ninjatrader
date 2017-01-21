package com.bn.ninjatrader.simulation.broker;

import com.bn.ninjatrader.common.boardlot.BoardLot;
import com.bn.ninjatrader.common.boardlot.BoardLotTable;
import com.bn.ninjatrader.common.data.Price;
import com.bn.ninjatrader.simulation.account.Account;
import com.bn.ninjatrader.simulation.data.BarData;
import com.bn.ninjatrader.simulation.order.MarketTime;
import com.bn.ninjatrader.simulation.order.Order;
import com.bn.ninjatrader.simulation.transaction.Transaction;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.testng.Assert.assertEquals;

/**
 * Created by Brad on 8/18/16.
 */
public class OrderExecutorTest {

  @Injectable
  private BoardLotTable boardLotTable;

  @Tested
  private OrderExecutor executor;

  private Account account;

  private final LocalDate now = LocalDate.of(2016, 1, 1);
  private final Price price = Price.builder().date(now).open(1).high(2).low(3).close(4).volume(1000).build();
  private final BarData barData = BarData.builder().price(price).build();
  private final BoardLot boardLot1 = BoardLot.newLot().lot(1000).tick(0.1).build();
  private final BoardLot boardLot2 = BoardLot.newLot().lot(100).tick(0.1).build();

  private final Order orderForMarketOpen = Order.buy().cashAmount(100000).at(MarketTime.OPEN).build();
  private final Order orderForMarketClose = Order.buy().cashAmount(100000).at(MarketTime.CLOSE).build();

  @BeforeMethod
  public void setup() {
    account = Account.withStartingCash(100000);
    account.addToPortfolio(Transaction.buy().price(1.0).shares(100000).build());

    new Expectations() {{
      boardLotTable.getBoardLot(1);
      result = boardLot1;
      minTimes = 0;

      boardLotTable.getBoardLot(2);
      result = boardLot1;
      minTimes = 0;

      boardLotTable.getBoardLot(5);
      result = boardLot2;
      minTimes = 0;
    }};
  }

  @Test
  public void testGetFulfilledPrice() {
    double fulfilledPrice = executor.getFulfilledPrice(orderForMarketOpen, barData);
    assertEquals(fulfilledPrice, 1.0);

    fulfilledPrice = executor.getFulfilledPrice(orderForMarketClose, barData);
    assertEquals(fulfilledPrice, 4.0);
  }

  @Test
  public void testGetNumberOfSharesCanBuy() {
    long numOfShares = executor.getNumOfSharesCanBuyWithAmount(100000, 1);
    assertEquals(numOfShares, 100000);

    numOfShares = executor.getNumOfSharesCanBuyWithAmount(100000, 2);
    assertEquals(numOfShares, 50000);

    numOfShares = executor.getNumOfSharesCanBuyWithAmount(99999, 1);
    assertEquals(numOfShares, 99000);

    numOfShares = executor.getNumOfSharesCanBuyWithAmount(99999, 5);
    assertEquals(numOfShares, 19900);
  }

  @Test
  public void testCalculateProfit() {
    double profit = executor.calculateProfit(account, 2.0);
    assertEquals(profit, 100000.0);

    profit = executor.calculateProfit(account, 2.5);
    assertEquals(profit, 150000.0);

    profit = executor.calculateProfit(account, 3.0);
    assertEquals(profit, 200000.0);
  }

  @Test
  public void testCalculateProfitAtLowerPrice() {
    double profit = executor.calculateProfit(account, 0.5);
    assertEquals(profit, -50000.0);
  }
}
