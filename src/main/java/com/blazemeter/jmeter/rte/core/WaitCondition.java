package com.blazemeter.jmeter.rte.core;

public abstract class WaitCondition {

  private long timeoutMillis;
  private long stableTimeoutMillis;

  public WaitCondition(long timeoutMillis, long stableTimeoutMillis) {
    this.timeoutMillis = timeoutMillis;
    this.stableTimeoutMillis = stableTimeoutMillis;
  }

  public long getTimeoutMillis() {
    return this.timeoutMillis;
  }

  public long getStableTimeoutMillis() {
    return this.stableTimeoutMillis;
  }

}
