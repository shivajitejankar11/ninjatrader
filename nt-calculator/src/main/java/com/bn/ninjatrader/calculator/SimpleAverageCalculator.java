package com.bn.ninjatrader.calculator;

import com.bn.ninjatrader.calculator.util.SimpleAverageCalculatingStack;
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
public class SimpleAverageCalculator extends AbstractCalculatorForPeriod {

  private static final Logger log = LoggerFactory.getLogger(SimpleAverageCalculator.class);

  @Override
  public List<Value> calc(List<Price> priceList, int period) {
    SimpleAverageCalculatingStack stack = SimpleAverageCalculatingStack.withFixedSize(period);
    List<Value> resultList = Lists.newArrayList();

    for (Price price : priceList) {
      stack.add(price);
      double average = stack.getValue();

      if (average > 0d) {
        Value value = new Value(price.getDate(), stack.getValue());
        resultList.add(value);
      }
    }
    return resultList;
  }
}