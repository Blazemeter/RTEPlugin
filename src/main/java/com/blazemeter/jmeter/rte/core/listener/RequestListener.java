package com.blazemeter.jmeter.rte.core.listener;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestListener<T extends RteProtocolClient> implements TerminalStateListener {

  private static final Logger LOG = LoggerFactory.getLogger(RequestListener.class);

  protected final T client;
  private final SampleResult result;
  private long lastResponseTime;
  private boolean receivedFirstResponse = false;

  public RequestListener(SampleResult result, T client) {
    this.result = result;
    this.client = client;
    lastResponseTime = result.currentTimeInMillis();
  }

  public void onTerminalStateChange() {
    if (!receivedFirstResponse) {
      receivedFirstResponse = true;
      result.latencyEnd();
    }
    lastResponseTime = result.currentTimeInMillis();
    if (LOG.isTraceEnabled()) {
      LOG.trace(client.getScreen().toString());
    }
  }

  public void stop() {
    result.setEndTime(lastResponseTime);
    client.removeTerminalStateListener(this);
  }

  @Override
  public void onException(Throwable e) {
  }

}
