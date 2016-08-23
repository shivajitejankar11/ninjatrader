package com.bn.ninjatrader.testplay.simulation.datafinder;

import com.bn.ninjatrader.common.data.DataType;
import com.bn.ninjatrader.common.data.Ichimoku;
import com.bn.ninjatrader.common.util.ListUtil;
import com.bn.ninjatrader.model.dao.period.FindRequest;
import com.bn.ninjatrader.service.indicator.IchimokuService;
import com.bn.ninjatrader.testplay.simulation.SimulationParameters;
import com.bn.ninjatrader.testplay.simulation.data.SimulationData;
import com.bn.ninjatrader.testplay.simulation.data.adaptor.IchimokuDataMapAdaptor;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collections;
import java.util.List;

import static com.bn.ninjatrader.common.data.DataType.*;
import static com.bn.ninjatrader.model.dao.period.FindRequest.forSymbol;

/**
 * Created by Brad on 8/20/16.
 */
@Singleton
public class IchimokuDataFinder implements DataFinder<Ichimoku> {

  private static final List<DataType> SUPPORTED_DATA_TYPES = Collections.unmodifiableList(
      Lists.newArrayList(CHIKOU, TENKAN, KIJUN, SENKOU_A, SENKOU_B));

  @Inject
  private IchimokuService ichimokuService;

  @Inject
  private IchimokuDataMapAdaptor dataMapAdaptor;

  @Override
  public List<SimulationData<Ichimoku>> find(SimulationParameters params, int requiredDataSize) {
    FindRequest findRequest = forSymbol(params.getSymbol()).from(params.getFromDate()).to(params.getToDate());
    List<Ichimoku> ichimokuList = ichimokuService.find(findRequest);
    ListUtil.fillToSize(ichimokuList, Ichimoku.empty(), requiredDataSize);
    return Lists.newArrayList(new SimulationData(ichimokuList, dataMapAdaptor));
  }

  @Override
  public List<DataType> getSupportedDataTypes() {
    return SUPPORTED_DATA_TYPES;
  }
}
