package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SilenceListener extends Tn3270ConditionWaiter<SilentWaitCondition> implements
    KeyboardStatusListener, CursorMoveListener, ScreenChangeListener {

  private static final Logger LOG = LoggerFactory
      .getLogger(com.blazemeter.jmeter.rte.protocols.tn3270.listeners.SilenceListener.class);

  public SilenceListener(SilentWaitCondition condition,
      ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, stableTimeoutExecutor);
    startStablePeriod();
  }

  private void handleReceivedEvent(String event) {
    LOG.debug("Restarting silent period since event received {}", event);
    startStablePeriod();
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
    handleReceivedEvent("screenChanged");
  }
}
