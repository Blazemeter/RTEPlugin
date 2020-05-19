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

public class ScreenTextListener extends Tn3270ConditionWaiter<TextWaitCondition> implements
    KeyboardStatusListener, CursorMoveListener, ScreenChangeListener {

  public ScreenTextListener(TextWaitCondition condition, Tn3270Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addCursorMoveListener(this);
    client.addKeyboardStatusListener(this);
    client.addScreenChangeListener(this);
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent keyboardStatusChangedEvent) {
    updateConditionState(keyboardStatusChangedEvent.getClass().getSimpleName());
  }

  @Override
  public void cursorMoved(int i, int i1, Field field) {
    updateConditionState(CURSOR_MOVED);

  }

  @Override
  public void screenChanged(ScreenWatcher screenWatcher) {
    updateConditionState(screenWatcher.getClass().getSimpleName());
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

}
