package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.bytezone.dm3270.TerminalClient;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.display.ScreenWatcher;
import java.awt.Dimension;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenTextListener extends Tn3270ConditionWaiter<TextWaitCondition> implements
    KeyboardStatusListener, CursorMoveListener, ScreenChangeListener {

  private static final Logger LOG = LoggerFactory.getLogger(ScreenTextListener.class);

  private boolean matched;

  public ScreenTextListener(TextWaitCondition condition,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler,
      TerminalClient client) {
    super(condition, stableTimeoutExecutor, exceptionHandler, client);
    client.addCursorMoveListener(this);
    client.addKeyboardStatusListener(this);
    client.addScreenChangeListener(this);
    checkIfScreenMatchesCondition();
    if (matched) {
      startStablePeriod();
    }
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent keyboardStatusChangedEvent) {
    handleReceivedEvent("keyboardStatusChanged");
  }

  @Override
  public void cursorMoved(int i, int i1, Field field) {
    handleReceivedEvent("cursorMoved");
  }

  @Override
  public void screenChanged(ScreenWatcher screenWatcher) {
    checkIfScreenMatchesCondition();
    handleReceivedEvent("screenChanged");
  }

  private void handleReceivedEvent(String event) {
    if (matched) {
      LOG.debug("Restart screen text stable period since received event {}", event);
      startStablePeriod();
    }
  }

  private void checkIfScreenMatchesCondition() {
    ScreenDimensions dimensions = client.getScreenDimensions();
    if (condition.matchesScreen(client.getScreenText(),
        new Dimension(dimensions.columns, dimensions.rows))) {
      LOG.debug("Found matching text in screen, now waiting for silent period.");
      matched = true;
    }
  }

  public void stop() {
    super.stop();
    client.removeCursorMoveListener(this);
    client.removeKeyboardStatusListener(this);
    client.removeScreenChangeListener(this);
  }
}
