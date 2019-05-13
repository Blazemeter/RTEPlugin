package com.blazemeter.jmeter.rte.core.wait;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConnectionEndWaiter implements TerminalStateListener {

  private final RteProtocolClient client;
  private final ExceptionHandler exceptionHandler;
  private final long timeoutMillis;
  private final CountDownLatch connected = new CountDownLatch(1);

  public ConnectionEndWaiter(RteProtocolClient client, ExceptionHandler exceptionHandler,
      long timeoutMillis) {
    this.client = client;
    this.exceptionHandler = exceptionHandler;
    this.timeoutMillis = timeoutMillis;
    client.addTerminalStateListener(this);
    if (!client.getScreen().isEmpty()) {
      connected.countDown();
      stop();
    }
  }

  @Override
  public void onTerminalStateChange() {
    connected.countDown();
  }

  @Override
  public void onException(Throwable e) {
    connected.countDown();
  }

  public void await() throws InterruptedException, TimeoutException, RteIOException {
    if (!connected.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException(
          "Timeout waiting for connection end after " + timeoutMillis + " ms");
    }
    exceptionHandler.throwAnyPendingError();
  }

  public void stop() {
    client.removeTerminalStateListener(this);
  }

}
