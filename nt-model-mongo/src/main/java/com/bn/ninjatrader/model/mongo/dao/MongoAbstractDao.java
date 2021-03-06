package com.bn.ninjatrader.model.mongo.dao;

import com.google.inject.Singleton;
import org.jongo.MongoCollection;

import java.time.LocalDate;

/**
 * Created by Brad on 4/30/16.
 */
@Singleton
public abstract class MongoAbstractDao {

  public static final LocalDate MINIMUM_FROM_DATE = LocalDate.of(1999, 1, 1);

  private final MongoCollection mongoCollection;

  public MongoAbstractDao(final MongoCollection mongoCollection) {
    this.mongoCollection = mongoCollection;
  }


  public MongoCollection getMongoCollection() {
    return mongoCollection;
  }
}
