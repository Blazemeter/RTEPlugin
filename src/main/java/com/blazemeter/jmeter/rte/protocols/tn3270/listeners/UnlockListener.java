package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import java.util.concurrent.ScheduledExecutorService;

public class UnlockListener extends Tn3270ConditionWaiter<SyncWaitCondition> implements
    KeyboardStatusListener {

  public UnlockListener(SyncWaitCondition condition, Tn3270Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addKeyboardStatusListener(this);
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent keyboardStatusChangedEvent) {
    updateConditionState(
        "keyboardStatusChanged: " + keyboardStatusChangedEvent.toString());
  }

  @Override
  public void stop() {
    super.stop();
    client.removeKeyboardStatusListener(this);
  }

  @Override
  protected boolean getCurrentConditionState() {
    return !client.isInputInhibited().get();
  }

}
