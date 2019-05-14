package com.blazemeter.jmeter.rte.core.wait;

import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
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

  public void await() throws InterruptedException, TimeoutException, RteIOException {
    if (!connected.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException(
          "Timeout waiting for connection end after " + timeoutMillis + " ms");
    }
  }

}
