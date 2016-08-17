package com.bn.ninjatrader.calculator;

import com.bn.ninjatrader.calculator.util.MeanCalculatingStack;
import com.bn.ninjatrader.common.data.Price;
import com.bn.ninjatrader.common.data.Value;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Brad on 5/28/16.
 */
@Singleton
public class MeanCalculator extends AbstractCalculatorForPeriod {

  private static final Logger log = LoggerFactory.getLogger(MeanCalculator.class);

  @Override
  public List<Value> calc(List<Price> priceList, int period) {
    MeanCalculatingStack meanCalculatingStack = MeanCalculatingStack.withFixedSize(period);
    List<Value> resultList = Lists.newArrayList();

    for (Price price : priceList) {
      meanCalculatingStack.add(price);

      double mean = meanCalculatingStack.getMean();

      if (mean > 0d) {
        Value value = new Value(price.getDate(), mean);
        resultList.add(value);
      }
    }
    return resultList;
  }
}
