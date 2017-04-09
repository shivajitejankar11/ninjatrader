package com.bn.ninjatrader.model.mongo.dao;

import com.bn.ninjatrader.common.type.TimeFrame;
import com.bn.ninjatrader.common.util.DateUtil;
import com.bn.ninjatrader.common.util.FixedList;
import com.bn.ninjatrader.model.dao.PriceDao;
import com.bn.ninjatrader.model.entity.Price;
import com.bn.ninjatrader.model.mongo.annotation.PriceCollection;
import com.bn.ninjatrader.model.mongo.document.MongoPriceDocument;
import com.bn.ninjatrader.model.mongo.util.Queries;
import com.bn.ninjatrader.model.request.FindBeforeDateRequest;
import com.bn.ninjatrader.model.request.SavePriceRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bn.ninjatrader.model.mongo.util.QueryParam.*;

/**
 * Created by Brad on 4/30/16.
 */
@Singleton
public class MongoPriceDao extends MongoAbstractDao implements PriceDao {
  private static final Logger LOG = LoggerFactory.getLogger(MongoPriceDao.class);

  private final Clock clock;

  @Inject
  public MongoPriceDao(@PriceCollection final MongoCollection priceCollection,
                       final Clock clock) {
    super(priceCollection);
    this.clock = clock;

    priceCollection.ensureIndex(Queries.createIndex(SYMBOL, TIMEFRAME, YEAR),
            "{unique: true}");
  }

  public FindPricesOperation findPrices() {
    return new FindPricesOperation(getMongoCollection());
  }

  @Override
  public void save(final SavePriceRequest req) {
    Collections.sort(req.getPrices());
    final List<LocalDate> removeDates = Lists.newArrayList();
    final List<Price> perYearPriceList = Lists.newArrayList();

    int currYear = 0;
    for (final Price price : req.getPrices()) {

      if (currYear != price.getDate().getYear()) {

        // Remove all old values w/ date in new values
        removeByDates(req, removeDates);

        // Save list of values to year
        saveByYear(req, currYear, perYearPriceList);

        removeDates.clear();
        perYearPriceList.clear();

        currYear = price.getDate().getYear();
      }

      perYearPriceList.add(price);
      removeDates.add(price.getDate());
    }

    // For the last year
    removeByDates(req, removeDates);
    saveByYear(req, currYear, perYearPriceList);
  }

  public void removeByDates(final SavePriceRequest req, final List<LocalDate> removeDates) {
    if (!removeDates.isEmpty()) {
      final List<String> dates = DateUtil.toListOfString(removeDates);

      getMongoCollection()
          .update(Queries.FIND_BY_SYMBOL_TIMEFRAME, req.getSymbol(), req.getTimeFrame())
          .multi()
          .with("{$pull: {data :{d: {$in: #}}}}", dates);
    }
  }

  private void saveByYear(final SavePriceRequest req,
                          final int year,
                          final List<Price> prices) {
    if (!prices.isEmpty()) {
      // Insert new values
      getMongoCollection()
          .update(Queries.FIND_BY_SYMBOL_TIMEFRAME_YEAR, req.getSymbol(), req.getTimeFrame(), year)
          .upsert()
          .with("{$push: { data: { $each: #, $sort: { d: 1}}}}", prices);
    }
  }

  @Override
  public Set<String> findAllSymbols() {
    final int thisYear = LocalDate.now(clock).getYear();
    final Set<String> symbols = Sets.newHashSet();

    try (final MongoCursor<MongoPriceDocument> cursor = getMongoCollection()
            .find(Queries.FIND_BY_TIMEFRAME_YEAR, TimeFrame.ONE_DAY, thisYear)
            .as(MongoPriceDocument.class)) {
      while (cursor.hasNext()) {
        symbols.add(cursor.next().getSymbol());
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return symbols;
  }

  @Override
  public List<Price> findBeforeDate(final FindBeforeDateRequest request) {
    final List<Price> bars = FixedList.withMaxSize(request.getNumOfValues());
    LocalDate fromDate = request.getBeforeDate().withDayOfYear(1);
    LocalDate toDate = request.getBeforeDate().minusDays(1);

    do {
      final List<Price> resultsPerYear = findPrices().withSymbol(request.getSymbol())
          .withTimeFrame(request.getTimeFrame())
          .from(fromDate)
          .to(toDate)
          .now();
      bars.clear();
      bars.addAll(resultsPerYear);
      fromDate = fromDate.minusYears(1);

      // If not enough data, reset the list and search again with a wider date range.
    } while (bars.size() < request.getNumOfValues() && fromDate.isAfter(MINIMUM_FROM_DATE));

    return bars;
  }

  /**
   * Builder for finding prices operation
   */
  public static final class FindPricesOperation implements PriceDao.FindPricesOperation {
    private final MongoCollection mongoCollection;
    private String symbol;
    private LocalDate from;
    private LocalDate to;
    private TimeFrame timeFrame = TimeFrame.ONE_DAY;

    public FindPricesOperation(final MongoCollection mongoCollection) {
      this.mongoCollection = mongoCollection;
    }

    @Override
    public FindPricesOperation withSymbol(final String symbol) {
      this.symbol = symbol;
      return this;
    }

    @Override
    public FindPricesOperation from(final LocalDate from) {
      this.from = from;
      return this;
    }

    @Override
    public FindPricesOperation to(final LocalDate to) {
      this.to = to;
      return this;
    }

    @Override
    public FindPricesOperation withTimeFrame(final TimeFrame timeFrame) {
      this.timeFrame = timeFrame;
      return this;
    }

    @Override
    public List<Price> now() {
      try (final MongoCursor<MongoPriceDocument> cursor = mongoCollection
          .find(Queries.FIND_BY_SYMBOL_TIMEFRAME_YEAR_RANGE, symbol,
              timeFrame,
              from.getYear(),
              to.getYear())
          .as(MongoPriceDocument.class)) {

        final List<Price> prices = Lists.newArrayList(cursor.iterator()).stream()
            .flatMap(doc -> doc.getData().stream())
            .filter(d -> !d.getDate().isBefore(from) && !d.getDate().isAfter(to))
            .sorted()
            .collect(Collectors.toList());

        return prices;
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
