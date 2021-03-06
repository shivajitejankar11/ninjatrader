package com.bn.ninjatrader.process.calc;

import com.bn.ninjatrader.calculator.WeeklyPriceCalculator;
import com.bn.ninjatrader.common.type.TimeFrame;
import com.bn.ninjatrader.common.util.DateUtil;
import com.bn.ninjatrader.model.dao.PriceDao;
import com.bn.ninjatrader.common.model.Price;
import com.bn.ninjatrader.model.util.DateObjUtil;
import com.bn.ninjatrader.process.request.CalcRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by Brad on 6/8/16.
 */
@Singleton
public class CalcWeeklyPriceProcess {
  private static final Logger LOG = LoggerFactory.getLogger(CalcWeeklyPriceProcess.class);

  private final WeeklyPriceCalculator calculator;
  private final PriceDao priceDao;

  @Inject
  public CalcWeeklyPriceProcess(final WeeklyPriceCalculator calculator,
                                final PriceDao priceDao) {
    this.calculator = calculator;
    this.priceDao = priceDao;
  }

  public void process(final CalcRequest calcRequest) {
    LOG.debug("with {}", calcRequest);
    for (final String symbol : calcRequest.getAllSymbols()) {
      final LocalDate priceFromDate = DateUtil.toStartOfWeek(calcRequest.getFromDate());
      final LocalDate priceToDate = calcRequest.getToDate();

      final List<Price> prices = priceDao.findPrices().withSymbol(symbol).from(priceFromDate).to(priceToDate).now();
      final List<Price> weeklyPrices = calculator.calc(prices);

      DateObjUtil.trimToDateRange(weeklyPrices, calcRequest.getFromDate(), calcRequest.getToDate());
      if (!weeklyPrices.isEmpty()) {
        priceDao.savePrices(weeklyPrices).withSymbol(symbol)
            .withTimeFrame(TimeFrame.ONE_WEEK)
            .now();
      }
    }
  }
}
