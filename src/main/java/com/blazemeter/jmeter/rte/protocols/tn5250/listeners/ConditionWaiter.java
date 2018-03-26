package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;

/**
 * An {@link XI5250EmulatorListener} which allows waiting for certain condition, and keeps in such
 * state for a given period of time.
 */
public abstract class ConditionWaiter<T extends WaitCondition> implements XI5250EmulatorListener {

  protected final T condition;
  protected final Tn5250Client client;
  private final CountDownLatch lock = new CountDownLatch(1);
  private final ScheduledExecutorService stableTimeoutExecutor;
  private ScheduledFuture stableTimeoutTask;
  private boolean ended;

  public ConditionWaiter(T condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor) {
    this.condition = condition;
    this.client = client;
    this.stableTimeoutExecutor = stableTimeoutExecutor;
  }

  @Override
  public void connecting(XI5250EmulatorEvent event) {
  }

  @Override
  public void connected(XI5250EmulatorEvent event) {
  }

  @Override
  public void disconnected(XI5250EmulatorEvent event) {
  }

  @Override
  public void stateChanged(XI5250EmulatorEvent event) {
    if (client.hasPendingError()) {
      cancelWait();
    }
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent event) {
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent event) {
  }

  @Override
  public void dataSended(XI5250EmulatorEvent event) {
  }

  protected synchronized void cancelWait() {
    ended = true;
    lock.countDown();
    endStablePeriod();
  }

  protected synchronized void startStablePeriod() {
    if (ended) {
      return;
    }
    endStablePeriod();
    stableTimeoutTask = stableTimeoutExecutor
        .schedule(lock::countDown, condition.getStableTimeoutMillis(), TimeUnit.MILLISECONDS);
  }

  protected synchronized void endStablePeriod() {
    if (stableTimeoutTask != null) {
      stableTimeoutTask.cancel(false);
    }
  }

  public void await() throws InterruptedException, TimeoutException, RteIOException {
    if (!lock.await(condition.getTimeoutMillis(), TimeUnit.MILLISECONDS)) {
      cancelWait();
      throw new TimeoutException(
          "Timeout waiting for " + condition.getDescription() + " after " + condition
              .getTimeoutMillis() + " millis. " +
              "Check if Timeout values of the 'Wait for' components " +
              "are greater than Stable time or Silent interval.");
    }
    client.throwAnyPendingError();
  }

  public void stop() {
    cancelWait();
  }

}
