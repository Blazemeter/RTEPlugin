package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorEvent;

/**
 * A {@link Tn5250ConditionWaiter} which allows waiting until the terminal is unlocked.
 */
public class UnlockListener extends Tn5250ConditionWaiter<SyncWaitCondition> {

  public UnlockListener(SyncWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, client, stableTimeoutExecutor);
  }

  @Override
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    if (client.hasPendingError()) {
      cancelWait();
      return;
    }
    switch (event.get5250Emulator().getState()) {
      case XI5250Emulator.ST_NORMAL_UNLOCKED:
        startStablePeriod();
        break;
      case XI5250Emulator.ST_NORMAL_LOCKED:
        endStablePeriod();
        break;
      default:
        //we ignore any other events
    }
  }

}
