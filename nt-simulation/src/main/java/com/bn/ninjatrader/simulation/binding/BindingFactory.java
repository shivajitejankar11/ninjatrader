package com.bn.ninjatrader.simulation.binding;

import com.bn.ninjatrader.logical.expression.operation.Variable;
import com.bn.ninjatrader.simulation.annotation.VarCalculatorMap;
import com.bn.ninjatrader.simulation.logicexpression.Variables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author bradwee2000@gmail.com
 */
@Singleton
public class BindingFactory {
  private static final Logger LOG = LoggerFactory.getLogger(BindingFactory.class);

  private final Map<String, Class> varCalculatorMap;
  private final List<Variable> excluded = Lists.newArrayList(Variables.MARKERS,
      Variables.ACCOUNT, Variables.BROKER, Variables.HISTORY,
      Variables.SYMBOL, Variables.PORTFOLIO, Variables.BOARDLOT);

  @Inject
  public BindingFactory(@VarCalculatorMap final Map<String, Class> varCalculatorMap) {
    this.varCalculatorMap = varCalculatorMap;
  }

  public BindingProvider createForVariable(final Variable variable) {
    try {
      if (varCalculatorMap.containsKey(variable.getDataType())) {
        return (BindingProvider) varCalculatorMap.get(variable.getDataType())
            .getConstructor(int.class)
            .newInstance(variable.getPeriod());
      } else {
        throw new IllegalArgumentException(String.format("Variable [%s] not supported.", variable));
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Set<BindingProvider> createForVariables(final Collection<Variable> variables) {
    variables.removeAll(excluded); // TODO, this is ugly, just add providers for each needed variable!!
    final Set<BindingProvider> set = new HashSet<>();
    for (final Variable variable : variables) {
      final BindingProvider bindingProvider = createForVariable(variable);
      set.add(bindingProvider);
    }
    return set;
  }
}
