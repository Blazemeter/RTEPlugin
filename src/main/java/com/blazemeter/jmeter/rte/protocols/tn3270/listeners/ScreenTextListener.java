package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenTextListener extends Tn3270ConditionWaiter<TextWaitCondition> implements
    KeyboardStatusListener, CursorMoveListener, ScreenChangeListener {

  private static final Logger LOG = LoggerFactory.getLogger(ScreenTextListener.class);

  public ScreenTextListener(TextWaitCondition condition, Tn3270Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addCursorMoveListener(this);
    client.addKeyboardStatusListener(this);
    client.addScreenChangeListener(this);
    if (getCurrentConditionState()) {
      LOG.debug(TextWaitCondition.SCREEN_MATCHES_TEXT_LOG_MESSAGE);
      startStablePeriod();
      conditionState.set(true);
    }
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent keyboardStatusChangedEvent) {
    handleReceivedEvent(keyboardStatusChangedEvent.getClass().getSimpleName());
  }

  @Override
  public void cursorMoved(int i, int i1, Field field) {
    handleReceivedEvent("cursor moved");

  }

  @Override
  public void screenChanged(ScreenWatcher screenWatcher) {
    handleReceivedEvent(screenWatcher.getClass().getSimpleName());
  }

  @Override
  public void stop() {
    super.stop();
    client.removeCursorMoveListener(this);
    client.removeKeyboardStatusListener(this);
    client.removeScreenChangeListener(this);
  }

  @Override
  protected boolean getCurrentConditionState() {
    return condition.matchesScreen(client.getScreen());
  }

  private void handleReceivedEvent(String event) {
    validateCondition(TextWaitCondition.RESTART_SCREEN_TEXT_WAIT_LOG_MESSAGE,
        TextWaitCondition.SCREEN_DO_NOT_MATCHES_TEXT_LOG_MESSAGE, event);
  }
}
