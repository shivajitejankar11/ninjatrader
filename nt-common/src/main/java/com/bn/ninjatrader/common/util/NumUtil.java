package com.bn.ninjatrader.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Brad on 6/10/16.
 */
public class NumUtil {

  private static final int DEFAULT_DECIMAL_PRECISION = 1000000;

  private NumUtil() {
  }

  public static double toDoubleOrDefault(String text, double defaultValue) {
    try {
      return toDouble(text);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public static double toDouble(String text) {
    if (StringUtils.isBlank(text)) {
      throw new IllegalArgumentException("Cannot convert to double. text must not be null or empty.");
    }
    text = text.replaceAll(",", "");
    return Double.parseDouble(text);
  }

  public static long toLongOrDefault(String text, long defaultValue) {
    try {
      return toLong(text);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public static long toLong(String text) {
    if (StringUtils.isBlank(text)) {
      throw new IllegalArgumentException("Cannot convert to long. text must not be null or empty.");
    }
    text = text.replaceAll(",", "");
    return (long) Double.parseDouble(text);
  }

  public static double plus(double lhs, double rhs) {
    long result = toBaseUnit(lhs) + toBaseUnit(rhs);
    return toActualUnit(result);
  }

  public static double minus(double lhs, double rhs) {
    long result = toBaseUnit(lhs) - toBaseUnit(rhs);
    return toActualUnit(result);
  }

  public static double divide(double lhs, double rhs) {
    long result = toBaseUnit(lhs / rhs);
    return toActualUnit(result);
  }

  public static double multiply(double lhs, double rhs) {
    long result = toBaseUnit(lhs * rhs);
    return toActualUnit(result);
  }

  private static long toBaseUnit(double value) {
    return Math.round(value * DEFAULT_DECIMAL_PRECISION);
  }

  private static double toActualUnit(long baseUnitValue) {
    return (double) baseUnitValue / DEFAULT_DECIMAL_PRECISION;
  }

  public static double trimPrice(double price) {
    return trim(price, 4);
  }

  public static double trim(double value, int decimalPlaces) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return value;
    }
    double precision = Math.pow(10, decimalPlaces);
    long trimmedValue = (long) (value * precision);
    return (double) trimmedValue / precision;
  }

  public static double round(double value, int decimalPlaces) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return value;
    }
    double precision = Math.pow(10, decimalPlaces);
    long rounded = Math.round(value * precision);
    return (double) rounded / precision;
  }

  public static double toPercent(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return value;
    }
    value = value * 100;
    return trim(value, 2);
  }

  public static int max(final List<Integer> values) {
    checkNotNull(values, "values must not be null.");
    checkArgument(values.size() > 0, "Array must have size > 0.");

    int max = Integer.MIN_VALUE;
    for (int value : values) {
      if (value > max) {
        max = value;
      }
    }
    return max;
  }
}
