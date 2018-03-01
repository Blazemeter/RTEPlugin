package com.blazemeter.jmeter.rte.protocols.tn5250;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;

public class UnlockListener implements XI5250EmulatorListener {

  private final long timeoutMillis;
  private final long stableTimeoutMillis;
  private final CountDownLatch lock = new CountDownLatch(1);
  private final ScheduledExecutorService stableTimeoutExecutor;
  private ScheduledFuture stableTimeoutTask;

  public UnlockListener(long timeoutMillis, long stableTimeoutMillis,
      ScheduledExecutorService stableTimeoutExecutor) {
    this.timeoutMillis = timeoutMillis;
    this.stableTimeoutMillis = stableTimeoutMillis;
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
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    switch (event.get5250Emulator().getState()) {
      case XI5250Emulator.ST_NORMAL_UNLOCKED:
        stableTimeoutTask = stableTimeoutExecutor
            .schedule(lock::countDown, stableTimeoutMillis, TimeUnit.MILLISECONDS);
        break;
      case XI5250Emulator.ST_NORMAL_LOCKED:
        if (stableTimeoutTask != null) {
          stableTimeoutTask.cancel(false);
        }
        break;
      default:
        //we ignore any other events
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

  public void await() throws InterruptedException, TimeoutException {
    if (!lock.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException(
          "Timeout when waiting for emulator to be unlocked after " + timeoutMillis + " millis");
    }
  }

}
