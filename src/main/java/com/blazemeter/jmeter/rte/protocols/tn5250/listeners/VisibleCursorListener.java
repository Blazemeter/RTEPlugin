package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ConditionWaiterTn5250} which allows waiting until the cursor shows up on the desired
 * position.
 */
public class VisibleCursorListener extends ConditionWaiterTn5250<CursorWaitCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(VisibleCursorListener.class);

  public VisibleCursorListener(CursorWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, client, stableTimeoutExecutor);
    if (condition.getPosition().equals(client.getCursorPosition())) {
      startStablePeriod();
    }
  }

  @Override
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    if (client.hasPendingError()) {
      cancelWait();
      return;
    }
    if (condition.getPosition().equals(client.getCursorPosition())) {
      LOG.debug("Cursor is in expected position, now waiting for it to remain for stable period");
      startStablePeriod();
    } else {
      LOG.debug("Cursor is not in expected position, canceling any stable period");
      endStablePeriod();
    }
  }

}
