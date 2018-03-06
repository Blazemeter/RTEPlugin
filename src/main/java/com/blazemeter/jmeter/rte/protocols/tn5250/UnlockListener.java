package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorEvent;

/**
 * An {@link ConditionWaiter} which allows waiting until the terminal is unlocked.
 */
public class UnlockListener extends ConditionWaiter<SyncWaitCondition> {

  public UnlockListener(SyncWaitCondition condition,
      ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, stableTimeoutExecutor);
  }

  @Override
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    if (hasPendingError(event)) {
      endWait();
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
