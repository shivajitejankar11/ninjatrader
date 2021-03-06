package com.bn.ninjatrader.model.mongo.dao;

import com.bn.ninjatrader.model.deprecated.Stock;
import com.bn.ninjatrader.model.mongo.annotation.StockCollection;
import com.bn.ninjatrader.model.mongo.util.Queries;
import com.bn.ninjatrader.model.mongo.util.QueryParam;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Created by Brad on 4/30/16.
 */
@Singleton
public class StockDao extends MongoAbstractDao {
  private static final Logger LOG = LoggerFactory.getLogger(StockDao.class);

  @Inject
  public StockDao(@StockCollection final MongoCollection mongoCollection) {
    super(mongoCollection);
    mongoCollection.ensureIndex(
            String.format("{%s : 1}", QueryParam.SYMBOL), "{unique: true}");
  }

  public Optional<Stock> findBySymbol(String symbol) {
    Stock data = getMongoCollection()
        .findOne(Queries.FIND_BY_SYMBOL, symbol)
        .as(Stock.class);
    return Optional.ofNullable(data);
  }

  public void save(Stock stock) {
    getMongoCollection()
        .update(Queries.FIND_BY_SYMBOL, stock.getSymbol())
        .upsert()
        .with(stock);
  }

  public void saveSymbolAndName(Stock stock) {
    getMongoCollection().update(Queries.FIND_BY_SYMBOL, stock.getSymbol())
        .upsert().with("{$set: {sym: #, nm: #}}", stock.getSymbol(), stock.getName());
  }

  public List<Stock> findWithNoSecurityId() {
    return Lists.newArrayList(getMongoCollection().find("{secId:0}").as(Stock.class).iterator());
  }

  public List<Stock> find() {
    return Lists.newArrayList(getMongoCollection().find().as(Stock.class).iterator());
  }

  public List<String> getAllSymbols() {
    List<String> symbols = Lists.newArrayList();
    for (Stock stock : find()) {
      symbols.add(stock.getSymbol());
    }
    return symbols;
  }
}
