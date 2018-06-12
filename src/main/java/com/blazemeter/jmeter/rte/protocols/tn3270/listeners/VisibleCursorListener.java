package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.bytezone.dm3270.TerminalClient;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisibleCursorListener extends Tn3270ConditionWaiter<CursorWaitCondition> implements
    CursorMoveListener {

  private static final Logger LOG = LoggerFactory.getLogger(VisibleCursorListener.class);

  public VisibleCursorListener(CursorWaitCondition condition,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler,
      TerminalClient client) {
    super(condition, stableTimeoutExecutor, exceptionHandler, client);
    client.addCursorMoveListener(this);
    if (condition.getPosition().equals(getCursorPosition())) {
      LOG.debug("Cursor is in expected position, now waiting for it to remain for stable period");
      startStablePeriod();
    }
  }

  private Position getCursorPosition() {
    return client.getCursorPosition()
        .map(p -> new Position(p.y, p.x))
        .orElse(null);
  }

  @Override
  public void cursorMoved(int i, int i1, Field field) {
    if (condition.getPosition().equals(getCursorPosition())) {
      LOG.debug("Cursor is in expected position, now waiting for it to remain for stable period");
      startStablePeriod();
    } else {
      LOG.debug("Cursor is not in expected position, canceling any stable period");
      endStablePeriod();
    }
  }

  @Override
  public void stop() {
    super.stop();
    client.removeCursorMoveListener(this);
  }

}
