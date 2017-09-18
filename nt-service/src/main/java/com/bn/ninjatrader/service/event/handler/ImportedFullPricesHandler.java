package com.bn.ninjatrader.service.event.handler;

import com.bn.ninjatrader.common.model.Algorithm;
import com.bn.ninjatrader.common.model.DailyQuote;
import com.bn.ninjatrader.messaging.Message;
import com.bn.ninjatrader.messaging.listener.MessageListener;
import com.bn.ninjatrader.model.dao.AlgorithmDao;
import com.bn.ninjatrader.queue.Task;
import com.bn.ninjatrader.queue.TaskDispatcher;
import com.bn.ninjatrader.service.annotation.cached.CachedDailyQuotes;
import com.bn.ninjatrader.service.store.ScanResultStore;
import com.bn.ninjatrader.simulation.scanner.ScanRequest;
import com.bn.ninjatrader.simulation.scanner.StockScanner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bradwee2000@gmail.com
 */
@Singleton
public class ImportedFullPricesHandler implements MessageListener<List<DailyQuote>> {
  private static final Logger LOG = LoggerFactory.getLogger(ImportedFullPricesHandler.class);

  private final StockScanner stockScanner;
  private final AlgorithmDao algorithmDao;
  private final ScanResultStore scanResultStore;
  private final List<DailyQuote> cachedDailyQuotes;
  private final TaskDispatcher taskDispatcher;

  @Inject
  public ImportedFullPricesHandler(final StockScanner stockScanner,
                                   final AlgorithmDao algorithmDao,
                                   final ScanResultStore scanResultStore,
                                   final TaskDispatcher taskDispatcher,
                                   @CachedDailyQuotes final List<DailyQuote> cachedDailyQuotes) {
    this.stockScanner = stockScanner;
    this.algorithmDao = algorithmDao;
    this.scanResultStore = scanResultStore;
    this.cachedDailyQuotes = cachedDailyQuotes;
    this.taskDispatcher = taskDispatcher;
  }

  @Override
  public void onMessage(final Message<List<DailyQuote>> message, final LocalDateTime publishTime) {
    final List<DailyQuote> quotes = message.getPayload();
    if (quotes.isEmpty()) {
      return;
    }

    final List<DailyQuote> diff = extractNewQuotes(quotes);
    if (diff.isEmpty()) {
      LOG.info("No new quotes found.");
      return;
    }

    LOG.info("Found {} new quotes.", diff.size());

    cachedDailyQuotes.clear();
    cachedDailyQuotes.addAll(quotes);

    processNewQuotes(diff);
  }

  private List<DailyQuote> extractNewQuotes(final List<DailyQuote> quotes) {
    final List<DailyQuote> diff = Lists.newArrayList(quotes);
    diff.removeAll(cachedDailyQuotes);
    return diff;
  }

  private void processNewQuotes(final List<DailyQuote> newQuotes) {
    LOG.info("Processing New Quotes: {}", newQuotes);

    final List<String> symbols = newQuotes.stream().map(q -> q.getSymbol()).collect(Collectors.toList());

    // Fetch all algorithms that are on realtime autoscan.
    final List<Algorithm> algorithms = algorithmDao.findAlgorithms().isAutoScan(true).now();

    LOG.info("Scanning {} symbols with {} algorithms.", newQuotes.size(), algorithms.size());

    // Scan new quotes w/ each algorithm
    algorithms.forEach(algorithm -> {
      // Only update what's already in cache, meaning user is live and checking on scan results.
      if (!scanResultStore.contains(algorithm.getId())) {
        return;
      }
      final ScanRequest scanRequest = ScanRequest.withAlgorithm(algorithm).symbols(symbols);

      taskDispatcher.submitTask(Task.withPath("/tasks/scan").payload(scanRequest));

//      final Map<String, ScanResult> scanResult = stockScanner.scan(scanRequest);
//      scanResultStore.merge(algorithm.getId(), scanResult);
    });
  }
}
