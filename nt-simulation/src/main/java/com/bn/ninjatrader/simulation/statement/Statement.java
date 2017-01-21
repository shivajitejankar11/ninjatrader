package com.bn.ninjatrader.simulation.statement;

import com.bn.ninjatrader.simulation.data.BarData;
import com.bn.ninjatrader.simulation.model.World;
import com.bn.ninjatrader.simulation.operation.Variable;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;

/**
 * @author bradwee2000@gmail.com
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BuyOrderStatement.class, name = "buy"),
    @JsonSubTypes.Type(value = SellOrderStatement.class, name = "sell"),
    @JsonSubTypes.Type(value = ConditionalStatment.class, name = "condition")
})
public interface Statement {

  void run(final World world, final BarData barData);

  Set<Variable> getVariables();
}