package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;

/**
 * A {@link Tn5250ConditionWaiter} which allows waiting until the cursor shows up on the desired
 * position.
 */
public class VisibleCursorListener extends Tn5250ConditionWaiter<CursorWaitCondition> {

  public VisibleCursorListener(CursorWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, client, stableTimeoutExecutor);
  }

  @Override
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    if (client.hasPendingError()) {
      cancelWait();
      return;
    }
    if (event.get5250Emulator().isCursorVisible()
        && event.get5250Emulator().getCursorRow() == condition.getPosition().getRow()
        && event.get5250Emulator().getCursorCol() == condition.getPosition().getColumn()) {
      startStablePeriod();
    } else {
      endStablePeriod();
    }
  }

}
