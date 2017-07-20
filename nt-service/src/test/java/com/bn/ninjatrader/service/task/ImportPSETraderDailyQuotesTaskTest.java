package com.bn.ninjatrader.service.task;

import com.bn.ninjatrader.dataimport.daily.PseTraderDailyPriceImporter;
import com.bn.ninjatrader.event.handler.MessageHandler;
import com.bn.ninjatrader.model.entity.DailyQuote;
import com.bn.ninjatrader.model.util.TestUtil;
import com.bn.ninjatrader.service.event.EventTypes;
import com.bn.ninjatrader.service.event.ImportedClosingPricesMessage;
import com.bn.ninjatrader.service.model.ImportQuotesRequest;
import com.bn.ninjatrader.service.util.EventIntegrationTest;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author bradwee2000@gmail.com
 */
public class ImportPSETraderDailyQuotesTaskTest extends EventIntegrationTest {
  private static final Logger LOG = LoggerFactory.getLogger(ImportPSETraderDailyQuotesTaskTest.class);
  private static final LocalDate now = LocalDate.of(2016, 2, 1);
  private static final PseTraderDailyPriceImporter importer = mock(PseTraderDailyPriceImporter.class);
  private static final Clock clock = TestUtil.fixedClock(now);
  private static final MessageHandler messageHandler = mock(MessageHandler.class);

  @Override
  protected Application configure() {
    final ResourceConfig resourceConfig = (ResourceConfig) super.configure();

    final ImportPSETraderDailyQuotesTask resource = new ImportPSETraderDailyQuotesTask(importer, clock);

    return resourceConfig.register(resource);
  }

  @Override
  public Multimap<String, MessageHandler> prepareSubscribers() {
    Multimap<String, MessageHandler> subscribers = ArrayListMultimap.create();
    subscribers.put(EventTypes.IMPORTED_FULL_PRICES, messageHandler);
    return subscribers;
  }

  @Before
  public void before() {
    reset(importer);
    reset(messageHandler);
  }

  @Test
  public void testImportWithDateArgs_shouldImportDataForGivenDates() {
    final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

    final LocalDate date1 = LocalDate.of(2016, 2, 1);
    final LocalDate date2 = LocalDate.of(2016, 2, 2);

    final ImportQuotesRequest request = new ImportQuotesRequest();
    request.setDates(Lists.newArrayList(date1, date2));

    final Response response = target("/task/import-pse-trader-quotes")
        .request()
        .post(Entity.json(request));

    assertThat(response.getStatus() == OK.getStatusCode());

    verify(importer).importData(captor.capture());
    assertThat(captor.getValue()).containsExactly(date1, date2);
  }

  @Test
  public void testImportWithNoDateArg_shouldImportDataForToday() {
    final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

    final Response response = target("/task/import-pse-trader-quotes")
        .request()
        .post(Entity.json(new ImportQuotesRequest()));

    assertThat(response.getStatus() == OK.getStatusCode());

    verify(importer).importData(captor.capture());
    assertThat(captor.getValue()).containsExactly(now);
  }

  @Test
  public void testImport_shouldSendImportEvent() {
    final ArgumentCaptor<ImportedClosingPricesMessage> captor =
        ArgumentCaptor.forClass(ImportedClosingPricesMessage.class);

    final ImportQuotesRequest request = new ImportQuotesRequest();
    request.setDates(Lists.newArrayList(now));

    final DailyQuote expectedQuote = new DailyQuote("MEG", now, 1, 2, 3, 4, 1000);

    when(importer.importData(anyCollection())).thenReturn(Lists.newArrayList(expectedQuote));

    target("/task/import-pse-trader-quotes").request().post(Entity.json(request));

    // Verify that message is sent and handled.
    verify(messageHandler).handle(captor.capture());

    // Verify payload contains expected quote
    assertThat(captor.getValue().getPayload()).containsExactly(expectedQuote);
  }
}
