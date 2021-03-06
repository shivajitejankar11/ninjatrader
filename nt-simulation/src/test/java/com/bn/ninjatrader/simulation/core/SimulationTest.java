package com.bn.ninjatrader.simulation.core;

import com.bn.ninjatrader.common.model.Price;
import com.bn.ninjatrader.simulation.algorithm.AlgorithmScript;
import com.bn.ninjatrader.simulation.algorithm.ScriptRunner;
import com.bn.ninjatrader.simulation.data.BarData;
import com.bn.ninjatrader.simulation.data.BarProducer;
import com.bn.ninjatrader.simulation.model.Account;
import com.bn.ninjatrader.simulation.model.Broker;
import com.bn.ninjatrader.simulation.model.History;
import com.bn.ninjatrader.simulation.model.SimulationContext;
import com.bn.ninjatrader.simulation.model.portfolio.Portfolio;
import com.bn.ninjatrader.simulation.model.stat.TradeStatistic;
import com.bn.ninjatrader.simulation.report.SimulationReport;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author bradwee2000@gmail.com
 */
public class SimulationTest {
  private static final Logger LOG = LoggerFactory.getLogger(SimulationTest.class);

  private final LocalDate date1 = LocalDate.of(2016, 2, 1);
  private final LocalDate date2 = LocalDate.of(2016, 2, 2);
  private final Price price1 = Price.builder().date(date1).open(1.2).high(1.4).low(1.1).close(1.3).volume(100000).build();
  private final Price price2 = Price.builder().date(date1).open(2.2).high(2.4).low(2.1).close(2.3).volume(200000).build();
  private final List<Price> prices = Lists.newArrayList(price1, price2);
  private final BarData bar1 = BarData.builder().price(price1).index(0).build();
  private final BarData bar2 = BarData.builder().price(price2).index(1).build();

  private AlgorithmScript algorithm;
  private SimulationRequest simRequest;
  private Account account;
  private Portfolio portfolio;
  private TradeStatistic tradeStatistic;
  private Broker broker;
  private BarProducer barProducer;
  private History history;
  private SimulationContext simulationContext;
  private ScriptRunner scriptRunner;

  private Simulation simulation;

  @Before
  public void before() {
    account = mock(Account.class);
    tradeStatistic = mock(TradeStatistic.class);
    portfolio = mock(Portfolio.class);
    broker = mock(Broker.class);
    barProducer = mock(BarProducer.class);
    history = mock(History.class);
    algorithm = mock(AlgorithmScript.class);
    scriptRunner = mock(ScriptRunner.class);

    simRequest = SimulationRequest.withSymbol("MEG")
        .startingCash(100000).from(date1).to(date2).algorithmScript(algorithm);

    when(account.getPortfolio()).thenReturn(portfolio);
    when(account.getTradeStatistic()).thenReturn(tradeStatistic);
    when(account.getLiquidCash()).thenReturn(100000d);
    when(portfolio.isEmpty()).thenReturn(true);
    when(barProducer.nextBar(anyString(), any(Price.class), any(SimulationContext.class))).thenReturn(bar1, bar2);
    when(algorithm.newRunner()).thenReturn(scriptRunner);

    simulationContext = SimulationContext.builder().account(account).broker(broker).pricesForSymbol("MEG", prices).history(history).build();

    simulation = new Simulation(simulationContext, simRequest, barProducer);
  }

  @Test
  public void testPlay_shouldCreateBarDataForEachPrice() {
    simulation.play();

    // Should forSymbol bar data twice. One for each price.
    verify(barProducer).nextBar("MEG", price1, simulationContext);
    verify(barProducer).nextBar("MEG", price2, simulationContext);
  }

  @Test
  public void testPlay_shouldSendBarDataToBrokerForEachPrice() {
    final ArgumentCaptor<BarData> barDataCaptor = ArgumentCaptor.forClass(BarData.class);

    simulation.play();

    // Verify barData is sent to broker for each price
    verify(broker, times(4)).processPendingOrders(barDataCaptor.capture());

    // Verify BarData values
    final List<BarData> barDataList = barDataCaptor.getAllValues();
    assertThat(barDataList.get(0).getPrice()).isEqualTo(price1);
    assertThat(barDataList.get(1).getPrice()).isEqualTo(price1);
    assertThat(barDataList.get(2).getPrice()).isEqualTo(price2);
    assertThat(barDataList.get(3).getPrice()).isEqualTo(price2);
  }

  @Test
  public void testPlay_shouldAddBarDataToHistoryForEachPrice() {
    final ArgumentCaptor<BarData> barDataCaptor = ArgumentCaptor.forClass(BarData.class);

    simulation.play();

    // Verify barData is added to history for each price
    verify(history, times(2)).add(barDataCaptor.capture());

    // Verify BarData values
    final List<BarData> barDataList = barDataCaptor.getAllValues();
    assertThat(barDataList.get(0).getPrice()).isEqualTo(price1);
    assertThat(barDataList.get(1).getPrice()).isEqualTo(price2);
  }

  @Test
  public void testPlayWithError_shouldReturnReportWithErrorMsg() {
    doThrow(new RuntimeException("test error"))
        .when(scriptRunner).onSimulationStart(any());

    final SimulationReport report = simulation.play();

    assertThat(report.getError()).isEqualTo("test error");
  }
}
