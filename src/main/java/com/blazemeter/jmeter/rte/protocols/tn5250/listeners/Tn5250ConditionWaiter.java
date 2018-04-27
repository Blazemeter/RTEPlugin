package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.ExtendedEmulator;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;

/**
 * An {@link XI5250EmulatorListener} which allows waiting for certain condition, and keeps in such
 * state for a given period of time.
 */
public abstract class Tn5250ConditionWaiter<T extends WaitCondition> extends
    ConditionWaiter<T> implements XI5250EmulatorListener {

  protected final Tn5250Client client;
  private final ExtendedEmulator em;

  public Tn5250ConditionWaiter(T condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExtendedEmulator em) {
    super(condition, stableTimeoutExecutor);
    this.em = em;
    this.client = client;
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

  public void await() throws InterruptedException, TimeoutException, RteIOException {
    super.await();
    client.throwAnyPendingError();
  }

  @Override
  public void stop() {
    super.stop();
    em.removeEmulatorListener(this);
  }
}
