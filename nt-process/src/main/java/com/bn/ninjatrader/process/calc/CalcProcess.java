package com.bn.ninjatrader.process.calc;

import com.bn.ninjatrader.process.request.CalcRequest;

/**
 * Created by Brad on 6/12/16.
 */
public interface CalcProcess {

  void processPrices(CalcRequest calcRequest);

  void processMissingBars(CalcRequest calcRequest);
}