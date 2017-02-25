package com.bn.ninjatrader.model.datastore.dao;

import com.bn.ninjatrader.model.datastore.entity.PriceDatastore;
import com.bn.ninjatrader.model.entity.Price;
import com.bn.ninjatrader.common.type.TimeFrame;
import com.bn.ninjatrader.model.util.DateObjUtil;
import com.bn.ninjatrader.model.datastore.document.PriceDocument;
import com.bn.ninjatrader.model.request.FindPriceRequest;
import com.bn.ninjatrader.model.request.SavePriceRequest;
import com.bn.ninjatrader.model.dao.PriceDao;
import com.bn.ninjatrader.model.request.FindBeforeDateRequest;
import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.appengine.repackaged.com.google.api.client.util.Preconditions.checkNotNull;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author bradwee2000@gmail.com
 */
@Singleton
public class PriceDaoDatastore implements PriceDao {
  private static final Logger LOG = LoggerFactory.getLogger(PriceDaoDatastore.class);

  private final Clock clock;

  @Inject
  public PriceDaoDatastore(final Clock clock) {
    this.clock = clock;
  }

  public Map<Key<PriceDocument>, PriceDocument> save(final PriceDocument document) {
    return ofy().save().entities(document).now();
  }

  @Override
  public void save(final SavePriceRequest request) {
    // Organize prices by year
    final Multimap<Integer, Price> pricesPerYear = ArrayListMultimap.create();
    for (final Price price : request.getPrices()) {
      pricesPerYear.put(price.getDate().getYear(), price);
    }

    // For each year, create createKey
    final List<Key<PriceDocument>> keys = Lists.newArrayList();
    for (int year : pricesPerYear.keySet()) {
      keys.add(createKey(request.getSymbol(), year, request.getTimeFrame()));
    }

    // Load documents where the prices belong
    final Map<Key<PriceDocument>, PriceDocument> documents = ofy().load().keys(keys);

    // Add prices to their respective documents
    for (int year : pricesPerYear.keySet()) {
      final Key<PriceDocument> key = createKey(request.getSymbol(), year, request.getTimeFrame());
      // Create new document if not existing
      if (documents.get(key) == null) {
        documents.put(key, new PriceDocument(request.getSymbol(), year, request.getTimeFrame()));
      }

      // Add prices to document. Must not have duplicate dates. Existing date will be overwritten.
      final Collection<Price> pricesToSave = pricesPerYear.get(year);
      final Map<LocalDate, PriceDatastore> docPrices = Maps.newHashMap();

      // Add document prices to a map, ensuring uniqueness by date.
      for (final Price price : documents.get(key).getData()) {
        docPrices.put(price.getDate(), (PriceDatastore) price);
      }

      // Add new prices to map, overwriting old prices of same date.
      for (final Price price : pricesToSave) {
        docPrices.put(price.getDate(), (PriceDatastore) price);
      }

      documents.get(key).setData(docPrices.values().stream().collect(Collectors.toList()));
    }

    // Save all documents
    ofy().save().entities(documents.values()).now();
  }

  @Override
  public List<Price> find(final FindPriceRequest request) {
    checkNotNull(request, "FindPriceRequest must not be null.");
    checkNotNull(request.getSymbol(), "FindPriceRequest.symbol must not be null.");
    checkNotNull(request.getFromDate(), "FindPriceRequest.fromDate must not be null.");
    checkNotNull(request.getToDate(), "FindPriceRequest.toDate must not be null.");

    final int fromYear = request.getFromDate().getYear();
    final int toYear = request.getToDate().getYear();

    // Create keys for each year
    final List<Key<PriceDocument>> keys = Lists.newArrayList();
    for (int i = fromYear; i <= toYear; i++) {
      keys.add(Key.create(PriceDocument.class, PriceDocument.id(request.getSymbol(), i, request.getTimeFrame())));
    }

    // Find documents
    final Map<Key<PriceDocument>, PriceDocument> results = ofy().load().keys(keys);

    // Collect prices from each document
    final List<Price> prices = Lists.newArrayList();
    for (final PriceDocument document : results.values()) {
      prices.addAll(document.getData());
    }

    Collections.sort(prices);

    DateObjUtil.trimToDateRange(prices, request.getFromDate(), request.getToDate());

    return prices;
  }

  @Override
  public Set<String> findAllSymbols() {
    final int thisYear = LocalDate.now(clock).getYear();
    final Set<String> symbols = Sets.newHashSet();

    final List<PriceDocument> documents = ofy().load().type(PriceDocument.class).filter("year = ", thisYear).list();
    for (PriceDocument document : documents) {
      symbols.add(document.getSymbol());
    }
    return symbols;
  }

  @Override
  public List<Price> findBeforeDate(FindBeforeDateRequest build) {
    return null;
  }

  private Key<PriceDocument> createKey(final String symbol, final int year, final TimeFrame timeFrame) {
    return Key.create(PriceDocument.class, PriceDocument.id(symbol, year, timeFrame));
  }
}