package com.bn.ninjatrader.thirdparty.psetrader;

import com.bn.ninjatrader.common.model.DailyQuote;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * @author bradwee2000@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PseTraderQuote {
  private static final Logger LOG = LoggerFactory.getLogger(PseTraderQuote.class);

  private final DailyQuote dailyQuote;

  public PseTraderQuote(@JsonProperty("s") final String symbol,
                        @JsonProperty("o") final double open,
                        @JsonProperty("h") final double high,
                        @JsonProperty("l") final double low,
                        @JsonProperty("c") final double close,
                        @JsonProperty("v") final long volume) {
    dailyQuote = new DailyQuote(symbol, null, open, high, low, close, volume);
  }

  public void setDate(final LocalDate date) {
    this.dailyQuote.setDate(date);
  }

  public DailyQuote getDailyQuote() {
    return dailyQuote;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("dailyQuote", dailyQuote)
        .toString();
  }
}
