package com.bn.ninjatrader.model.mongo.dao;

import com.bn.ninjatrader.model.deprecated.Value;
import com.bn.ninjatrader.model.request.FindBeforeDateRequest;
import com.bn.ninjatrader.model.request.SaveRequest;
import org.jongo.MongoCollection;

import java.util.List;

/**
 * Created by Brad on 8/16/16.
 */
public interface ValueDao<T extends Value> {

  void save(final SaveRequest saveRequest);


  MongoCollection getMongoCollection();

  List<T> findBeforeDate(final FindBeforeDateRequest request);
}
