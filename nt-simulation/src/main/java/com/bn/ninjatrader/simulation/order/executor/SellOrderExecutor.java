package com.bn.ninjatrader.simulation.order.executor;

import com.bn.ninjatrader.common.boardlot.BoardLotTable;
import com.bn.ninjatrader.common.util.NumUtil;
import com.bn.ninjatrader.simulation.data.BarData;
import com.bn.ninjatrader.simulation.model.Account;
import com.bn.ninjatrader.simulation.model.Portfolio;
import com.bn.ninjatrader.simulation.model.SimContext;
import com.bn.ninjatrader.simulation.order.PendingOrder;
import com.bn.ninjatrader.simulation.order.type.OrderType;
import com.bn.ninjatrader.simulation.transaction.SellTransaction;
import com.bn.ninjatrader.simulation.transaction.Transaction;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Brad on 8/13/16.
 */
@Singleton
public class SellOrderExecutor extends OrderExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(SellOrderExecutor.class);

  @Inject
  public SellOrderExecutor(final BoardLotTable boardLotTable) {
    super(boardLotTable);
  }

  public SellTransaction execute(final PendingOrder pendingOrder, final BarData currentBarData) {
    checkConditions(pendingOrder, currentBarData);

    final SimContext simContext = currentBarData.getSimContext();
    final Account account = simContext.getAccount();
    final Portfolio portfolio = account.getPortfolio();
    final OrderType orderType = pendingOrder.getOrderType();
    final BarData submittedBarData = pendingOrder.getSubmittedBarData();
    final String symbol = currentBarData.getSymbol();
    final long numOfShares = portfolio.getTotalShares(symbol);
    final double sellPrice = orderType.getFulfilledPrice(submittedBarData, currentBarData);
    final double avgPrice = portfolio.getAvgPrice(symbol);
    final double profit = NumUtil.multiply(sellPrice - avgPrice, numOfShares);
    final double profitPcnt = NumUtil.divide(profit, portfolio.getEquityValue(symbol));

    portfolio.fulfillCommittedShares(symbol, numOfShares);

    final SellTransaction sellTransaction = Transaction.sell()
        .date(currentBarData.getPrice().getDate())
        .symbol(pendingOrder.getSymbol())
        .price(sellPrice)
        .shares(numOfShares)
        .profit(profit)
        .profitPcnt(profitPcnt)
        .barIndex(currentBarData.getIndex())
        .build();

    account.addCash(sellTransaction.getValue());

    return sellTransaction;
  }
}
