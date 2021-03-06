package com.bn.ninjatrader.service.task;

import com.bn.ninjatrader.simulation.algorithm.AlgorithmScript;
import com.bn.ninjatrader.simulation.algorithm.AlgorithmService;
import com.bn.ninjatrader.simulation.core.Simulation;
import com.bn.ninjatrader.simulation.core.SimulationFactory;
import com.bn.ninjatrader.simulation.core.SimulationRequest;
import com.bn.ninjatrader.simulation.model.stat.EmptyTradeStatistic;
import com.bn.ninjatrader.simulation.report.SimulationReport;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author bradwee2000@gmail.com
 */
public class RunSimulationTaskTest extends JerseyTest {
  private static final Logger LOG = LoggerFactory.getLogger(RunSimulationTaskTest.class);

  private static final SimulationFactory simulationFactory = mock(SimulationFactory.class);
  private static final AlgorithmService algorithmService = mock(AlgorithmService.class);

  @Captor
  private ArgumentCaptor<SimulationRequest> requestCaptor;

  private SimulationReport simulationReport;

  @Override
  protected Application configure() {
    final RunSimulationTask task = new RunSimulationTask(simulationFactory, algorithmService);
    return new ResourceConfig().register(task);
  }

  @Before
  public void before() {
    reset(simulationFactory);
    reset(algorithmService);

    initMocks(this);

    simulationReport = SimulationReport.builder()
        .startingCash(100_000)
        .endingCash(200_000)
        .tradeStatistics(EmptyTradeStatistic.instance())
        .build();

    final Simulation simulation = mock(Simulation.class);

    when(algorithmService.findById(anyString())).thenReturn(mock(AlgorithmScript.class));
    when(simulationFactory.create(any())).thenReturn(simulation);
    when(simulation.play()).thenReturn(simulationReport);
  }

  @Test
  public void testRun_shouldReturnSimulationReport() {
    final SimulationReport report = target("/tasks/simulation/run")
        .queryParam("symbol", "MEG")
        .queryParam("algoId", "algoId")
        .request()
        .get(SimulationReport.class);

    assertThat(report).isNotNull();
    assertThat(report.getStartingCash()).isEqualTo(100000);
    assertThat(report.getEndingCash()).isEqualTo(200000);
  }

  @Test
  public void testRunWithParams_shouldUseGivenParams() {
    target("/tasks/simulation/run")
        .queryParam("symbol", "MEG")
        .queryParam("from", "20160101")
        .queryParam("to", "20161231")
        .queryParam("algoId", "dtRKje03")
        .queryParam("isDebug", "true")
        .request().get();

    verify(simulationFactory).create(requestCaptor.capture());

    final SimulationRequest req = requestCaptor.getValue();
    assertThat(req.getSymbol()).isEqualTo("MEG");
    assertThat(req.getFrom()).isEqualTo(LocalDate.of(2016, 1, 1));
    assertThat(req.getTo()).isEqualTo(LocalDate.of(2016, 12, 31));
    assertThat(req.getAlgorithmScript()).isNotNull();
    assertThat(req.isDebug()).isTrue();
  }

  @Test
  public void testRunWithNoInputSymbol_shouldReturn400Error() {
    final Response response = target("/tasks/simulation/run").request().get();
    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void testRunWithLowercaseSymbol_shouldConvertToUppercase() {
    target("/tasks/simulation/run")
        .queryParam("symbol", "meg")
        .queryParam("algoId", "dtRKje03")
        .request().get();

    verify(simulationFactory).create(requestCaptor.capture());

    final SimulationRequest req = requestCaptor.getValue();
    assertThat(req.getSymbol()).isEqualTo("MEG");
  }

  @Test
  public void testRunWithNoInputAlgoId_shouldReturn400Error() {
    final Response response = target("/tasks/simulation/run")
        .queryParam("symbol", "MEG")
        .request().get();
    assertThat(response.getStatus()).isEqualTo(400);
  }
}
