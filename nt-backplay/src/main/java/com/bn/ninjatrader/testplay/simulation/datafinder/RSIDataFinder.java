package com.bn.ninjatrader.testplay.simulation.datafinder;

import com.bn.ninjatrader.common.data.RSIValue;
import com.bn.ninjatrader.common.data.Value;
import com.bn.ninjatrader.common.util.ListUtil;
import com.bn.ninjatrader.model.dao.RSIDao;
import com.bn.ninjatrader.model.request.FindRequest;
import com.bn.ninjatrader.testplay.simulation.SimulationParams;
import com.bn.ninjatrader.testplay.simulation.adaptor.RSIDataMapAdaptor;
import com.bn.ninjatrader.testplay.simulation.data.DataType;
import com.bn.ninjatrader.testplay.simulation.data.SimulationData;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.bn.ninjatrader.model.request.FindRequest.forSymbol;

/**
 * Relative Strength Index DataFinder
 *
 * Created by Brad on 8/20/16.
 */
@Singleton
public class RSIDataFinder implements DataFinder<Value> {

  private static final List<DataType> SUPPORTED_DATA_TYPES;
  private static final String DATATYPE_PREFIX = "RSI_";

  static {
    List<DataType> supportedDataTypes = Lists.newArrayList();
    for (DataType dataType : DataType.values()) {
      if (dataType.name().startsWith(DATATYPE_PREFIX)) {
        supportedDataTypes.add(dataType);
      }
    }
    SUPPORTED_DATA_TYPES = Collections.unmodifiableList(supportedDataTypes);
  }

  @Inject
  private RSIDao rsiDao;

  @Override
  public List<SimulationData<Value>> find(SimulationParams params, int requiredDataSize) {
    List<SimulationData<Value>> simulationDataList = Lists.newArrayList();
    FindRequest findRequest = forSymbol(params.getSymbol()).from(params.getFromDate()).to(params.getToDate());
    Set<DataType> dataTypes = params.getDataTypes();

    for (DataType dataType : dataTypes) {
      if (dataType.name().startsWith(DATATYPE_PREFIX)) {
        int period = dataType.getPeriod();
        findRequest.period(period);
        List<RSIValue> valueList = rsiDao.find(findRequest);
        ListUtil.fillToSize(valueList, RSIValue.empty(), requiredDataSize);
        simulationDataList.add(new SimulationData(valueList, RSIDataMapAdaptor.forPeriod(period)));
      }
    }
    return simulationDataList;
  }

  @Override
  public List<DataType> getSupportedDataTypes() {
    return SUPPORTED_DATA_TYPES;
  }

}
