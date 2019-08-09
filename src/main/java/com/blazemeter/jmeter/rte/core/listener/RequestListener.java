package com.blazemeter.jmeter.rte.core.listener;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestListener<T extends RteProtocolClient> implements TerminalStateListener {

  private static final Logger LOG = LoggerFactory.getLogger(RequestListener.class);

  protected final T client;
  private final RteSampleResultBuilder resultBuilder;
  private long lastResponseTime;
  private boolean receivedFirstResponse = false;

  public RequestListener(RteSampleResultBuilder resultBuilder, T client) {
    this.resultBuilder = resultBuilder;
    this.client = client;
    lastResponseTime = resultBuilder.getCurrentTimeInMillis();
  }

  @Override
  public void onTerminalStateChange() {
    if (!receivedFirstResponse) {
      receivedFirstResponse = true;
      resultBuilder.withLatencyEndNow();
    }
    lastResponseTime = resultBuilder.getCurrentTimeInMillis();
    if (LOG.isTraceEnabled()) {
      LOG.trace(client.getScreen().toString());
    }
  }

  public void stop() {
    resultBuilder.withEndTime(lastResponseTime);
    client.removeTerminalStateListener(this);
  }

  @Override
  public void onException(Throwable e) {
  }

}
