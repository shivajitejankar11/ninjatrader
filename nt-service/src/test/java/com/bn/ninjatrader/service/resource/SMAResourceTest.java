package com.bn.ninjatrader.service.resource;

import com.bn.ninjatrader.common.data.Ichimoku;
import com.bn.ninjatrader.common.util.TestUtil;
import com.bn.ninjatrader.model.request.FindRequest;
import com.bn.ninjatrader.service.indicator.IchimokuService;
import com.bn.ninjatrader.service.model.IchimokuResponse;
import com.bn.ninjatrader.service.provider.LocalDateParamConverterProvider;
import com.google.common.collect.Lists;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author bradwee2000@gmail.com
 */
public class SMAResourceTest {

  private static final IchimokuService ichimokuService = mock(IchimokuService.class);
  private static final LocalDate date1 = LocalDate.of(2016, 2, 1);
  private static final LocalDate date2 = LocalDate.of(2016, 2, 5);
  private static final Ichimoku ichimoku1 = new Ichimoku(date1, 1, 2, 3, 4, 5);
  private static final Ichimoku ichimoku2 = new Ichimoku(date2, 5, 6, 7, 8, 9);
  private static final Clock fixedClock = TestUtil.fixedClock(date1);

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new IchimokuResource(ichimokuService, fixedClock))
      .addProvider(LocalDateParamConverterProvider.class)
      .build();

  @Before
  public void before() {
    when(ichimokuService.find(any(FindRequest.class)))
        .thenReturn(Lists.newArrayList(ichimoku1, ichimoku2));
  }

  @After
  public void after() {
    Mockito.reset(ichimokuService);
  }

  @Test
  public void requestIchimoku_shouldReturnList() {
    IchimokuResponse response = resources.client()
        .target("/ichimoku/MEG?timeframe=ONE_WEEK&from=20160101&to=20171231")
        .request()
        .get(IchimokuResponse.class);

    assertThat(response.getValues()).hasSize(2);
    assertThat(response.getValues().get(0)).isEqualTo(ichimoku1);
    assertThat(response.getValues().get(1)).isEqualTo(ichimoku2);
  }

  @Test
  public void requestIchimokuWithNoDataFound_shouldReturnEmptyList() {
    when(ichimokuService.find(any(FindRequest.class)))
        .thenReturn(Collections.emptyList());

    IchimokuResponse response = resources.client()
        .target("/ichimoku/MEG")
        .request()
        .get(IchimokuResponse.class);

    assertThat(response.getValues()).isNotNull();
    assertThat(response.getValues()).isEmpty();
  }
}