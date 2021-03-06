package com.bn.ninjatrader.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * @author bradwee2000@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Algorithm {
  public static final Builder builder() {
    return new Builder();
  }

  @JsonProperty("algorithmId")
  private final String id;

  @JsonProperty("userId")
  private final String userId;

  @JsonProperty("algorithm")
  private final String algorithm;

  @JsonProperty("description")
  private final String description;

  @JsonProperty("isAutoScan")
  private final boolean isAutoScan;

  public Algorithm(@JsonProperty("algorithmId") final String id,
                   @JsonProperty("userId") final String userId,
                   @JsonProperty("algorithm") final String algorithm,
                   @JsonProperty("description") final String description,
                   @JsonProperty("isAutoScan") final boolean isAutoScan) {
    this.id = id;
    this.userId = userId;
    this.algorithm = algorithm;
    this.description = description;
    this.isAutoScan = isAutoScan;
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public String getDescription() {
    return description;
  }

  public boolean isAutoScan() {
    return isAutoScan;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Algorithm algorithm1 = (Algorithm) o;
    return isAutoScan == algorithm1.isAutoScan &&
        Objects.equal(id, algorithm1.id) &&
        Objects.equal(userId, algorithm1.userId) &&
        Objects.equal(algorithm, algorithm1.algorithm) &&
        Objects.equal(description, algorithm1.description);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, userId, algorithm, description, isAutoScan);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("userId", userId)
        .add("algorithm", algorithm)
        .add("description", description)
        .add("isAutoScan", isAutoScan)
        .toString();
  }

  /**
   * Builder
   */
  public static final class Builder {
    private String id;
    private String userId;
    private String algorithm;
    private String description;
    private boolean isAutoScan = false;

    public Builder algoId(final String id) {
      this.id = id;
      return this;
    }

    public Builder userId(final String userId) {
      this.userId = userId;
      return this;
    }

    public Builder algorithm(final String algorithm) {
      this.algorithm = algorithm;
      return this;
    }

    public Builder description(final String description) {
      this.description = description;
      return this;
    }

    public Builder isAutoScan(final boolean isAutoScan) {
      this.isAutoScan = isAutoScan;
      return this;
    }

    public Algorithm build() {
      return new Algorithm(id, userId, algorithm, description, isAutoScan);
    }
  }
}
