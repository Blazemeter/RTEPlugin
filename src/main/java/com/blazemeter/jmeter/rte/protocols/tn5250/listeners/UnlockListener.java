package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;

/**
 * A {@link Tn5250ConditionWaiter} which allows waiting until the terminal is unlocked.
 */
public class UnlockListener extends Tn5250ConditionWaiter<SyncWaitCondition> {

  public UnlockListener(SyncWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
  }

  @Override
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    updateConditionState(event.toString());
  }

  @Override
  protected boolean getCurrentConditionState() {
    return !client.isInputInhibited();
  }
}
