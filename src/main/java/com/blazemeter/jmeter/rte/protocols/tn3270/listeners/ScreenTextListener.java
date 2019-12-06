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
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenTextListener extends Tn3270ConditionWaiter<TextWaitCondition> implements
    KeyboardStatusListener, CursorMoveListener, ScreenChangeListener {

  private static final Logger LOG = LoggerFactory.getLogger(ScreenTextListener.class);
  private boolean lastMatched;
  private BooleanSupplier matched = () -> condition.matchesScreen(client.getScreen());

  public ScreenTextListener(TextWaitCondition condition, Tn3270Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addCursorMoveListener(this);
    client.addKeyboardStatusListener(this);
    client.addScreenChangeListener(this);
    if (matched.getAsBoolean()) {
      startStablePeriod();
      lastMatched = true;
    }
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent keyboardStatusChangedEvent) {
    handleEvent("keyboardStatusChangedEvent");
  }

  @Override
  public void cursorMoved(int i, int i1, Field field) {
    handleEvent("cursorMoved");
  }

  @Override
  public void screenChanged(ScreenWatcher screenWatcher) {
    handleEvent("screenWatcher");
  }

  private synchronized void handleEvent(String event) {
    if (lastMatched) {
      if (matched.getAsBoolean()) {
        startStablePeriod();
        LOG.debug("Restart screen text stable period since received event {}", event);
      } else {
        endStablePeriod();
        lastMatched = false;
      }
      return;
    }
    if (matched.getAsBoolean()) {
      startStablePeriod();
      lastMatched = true;
    }
  }

  @Override
  public void stop() {
    super.stop();
    client.removeCursorMoveListener(this);
    client.removeKeyboardStatusListener(this);
    client.removeScreenChangeListener(this);
  }

}
