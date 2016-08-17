package com.bn.ninjatrader.testplay.operation;

import com.bn.ninjatrader.testplay.parameter.BarParameters;
import com.bn.ninjatrader.testplay.type.DataType;

/**
 * Created by Brad on 8/2/16.
 */
public class UnaryOperation implements Operation {

  private DataType dataType;
  private double constant;

  public static UnaryOperation of(double constantValue) {
    return new UnaryOperation(constantValue);
  }

  public static UnaryOperation of(DataType dataType) {
    return new UnaryOperation(dataType);
  }

  public UnaryOperation(DataType dataType) {
    this.dataType = dataType;
  }

  public UnaryOperation(double constantValue) {
    this.dataType = DataType.CONSTANT;
    this.constant = constantValue;
  }

  @Override
  public double getValue(BarParameters barParameters) {
    if (dataType == DataType.CONSTANT){
      return constant;
    }
    return barParameters.get(dataType);
  }
}
