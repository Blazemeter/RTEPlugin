package com.blazemeter.jmeter.rte.core.wait;

public class DisconnectWaitCondition extends WaitCondition {

  public DisconnectWaitCondition(long timeoutMillis) {
    super(timeoutMillis, 0);
  }

  @Override
  public String getDescription() {
    return "server to be disconnected";
  }

  @Override
  public String toString() {
    return "DisconnectWaitCondition{" +
        "timeoutMillis=" + timeoutMillis +
        '}';
  }
}
