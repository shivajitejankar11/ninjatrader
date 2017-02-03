package com.bn.ninjatrader.simulation.order.executor;

import com.bn.ninjatrader.common.boardlot.BoardLot;
import com.bn.ninjatrader.common.boardlot.BoardLotTable;
import com.bn.ninjatrader.common.data.Price;
import com.bn.ninjatrader.simulation.data.BarData;
import com.bn.ninjatrader.simulation.model.Account;
import com.bn.ninjatrader.simulation.order.BuyOrder;
import com.bn.ninjatrader.simulation.order.Order;
import com.bn.ninjatrader.simulation.order.PendingOrder;
import com.bn.ninjatrader.simulation.order.type.OrderTypes;
import com.bn.ninjatrader.simulation.transaction.BuyTransaction;
import com.bn.ninjatrader.simulation.transaction.TransactionType;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Brad on 8/18/16.
 */
public class BuyOrderExecutorTest {


  private final LocalDate now = LocalDate.of(2016, 1, 1);
  private final Price price = Price.builder().date(now).open(1).high(2).low(3).close(4).volume(1000).build();
  private final BarData barData = BarData.builder().price(price).build();
  private final BarData submittedBarData = BarData.builder().price(price).build();
  private final BoardLot boardLot = BoardLot.newLot().lot(1000).tick(0.1).build();
  private final Order order = BuyOrder.builder().cashAmount(100000).type(OrderTypes.marketOpen()).build();
  private final PendingOrder pendingOrder = PendingOrder.of(order, submittedBarData);

  private BoardLotTable boardLotTable;
  private Account account;

  private BuyOrderExecutor executor;

  @Before
  public void setup() {
    account = Account.withStartingCash(100000);
    boardLotTable = mock(BoardLotTable.class);

    when(boardLotTable.getBoardLot(anyDouble())).thenReturn(boardLot);

    executor = new BuyOrderExecutor(boardLotTable);
  }

  @Test
  public void testExecute() {
    final BuyTransaction transaction = executor.execute(account, pendingOrder, barData);

    assertValidTransaction(transaction);
    assertBuyAddedToAccount();
  }

  private void assertValidTransaction(BuyTransaction transaction) {
    assertThat(transaction).isNotNull();
    assertThat(transaction.getDate()).isEqualTo(now);
    assertThat(transaction.getNumOfShares()).isEqualTo(100000);
    assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.BUY);
    assertThat(transaction.getValue()).isEqualTo(100000.0);
  }

  private void assertBuyAddedToAccount() {
    assertThat(account.getLiquidCash()).isEqualTo(0d);
    assertThat(account.getPortfolio().getTotalShares()).isEqualTo(100000);
    assertThat(account.getPortfolio().getAvgPrice()).isEqualTo(1.0);
  }
}
