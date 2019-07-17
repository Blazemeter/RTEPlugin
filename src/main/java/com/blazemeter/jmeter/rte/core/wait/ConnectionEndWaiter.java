package com.blazemeter.jmeter.rte.core.wait;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConnectionEndWaiter {

  private final long timeoutMillis;
  private final CountDownLatch connected = new CountDownLatch(1);

  public ConnectionEndWaiter(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }

  public void stop() {
    connected.countDown();
  }

  public void await() throws InterruptedException, TimeoutException {
    if (!connected.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException(
          "Timeout waiting for connection to be established after " + timeoutMillis + " ms");
    }
  }

}
