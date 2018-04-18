package com.blazemeter.jmeter.rte.core.listener;

import com.blazemeter.jmeter.rte.core.BaseProtocolClient;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RequestListener<T extends BaseProtocolClient> {

  private static final Logger LOG = LoggerFactory.getLogger(RequestListener.class);

  protected final T client;
  private Instant startTime = Instant.now();
  private Instant firstResponseTime = Instant.now();
  private long lastResponseTime = System.currentTimeMillis();
  private boolean receivedFirstResponse = false;

  public RequestListener(T client) {
    this.client = client;
  }

  public long getLatency() {
    return Duration.between(startTime, firstResponseTime).toMillis();
  }

  public long getEndTime() {
    return lastResponseTime;
  }

  protected void newScreenReceived() {
    if (!receivedFirstResponse) {
      receivedFirstResponse = true;
      firstResponseTime = Instant.now();
    }
    lastResponseTime = System.currentTimeMillis();
    if (LOG.isTraceEnabled()) {
      LOG.trace(client.getScreen());
    }
  }

  public abstract void stop();

}
