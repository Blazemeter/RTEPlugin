package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Tn5250ConditionWaiter} which allows waiting until the cursor shows up on the desired
 * position.
 */
public class VisibleCursorListener extends Tn5250ConditionWaiter<CursorWaitCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(VisibleCursorListener.class);

  public VisibleCursorListener(CursorWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    if (getCurrentConditionState()) {
      LOG.debug(CursorWaitCondition.EXPECTED_CURSOR_POSITION);
      startStablePeriod();
      conditionState.set(true);
    }
  }

  @Override
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    validateCondition(CursorWaitCondition.EXPECTED_CURSOR_POSITION,
        CursorWaitCondition.NOT_EXPECTED_CURSOR_POSITION, event.toString());
  }

  @Override
  protected boolean getCurrentConditionState() {
    return condition.getPosition().equals(client.getCursorPosition().orElse(null));
  }
}
