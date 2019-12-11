package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnlockListener extends Tn3270ConditionWaiter<SyncWaitCondition> implements
    KeyboardStatusListener {

  private static final Logger LOG = LoggerFactory.getLogger(
      UnlockListener.class);

  public UnlockListener(SyncWaitCondition condition, Tn3270Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addKeyboardStatusListener(this);
    if (getCurrentConditionState()) {
      LOG.debug(SyncWaitCondition.INPUT_INHIBITED_LOG_MESSAGE);
      startStablePeriod();
      conditionState.set(true);
    }
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent keyboardStatusChangedEvent) {
    LOG.debug("keyboardStatusChanged {}", keyboardStatusChangedEvent.toString());
    validateCondition(SyncWaitCondition.INPUT_INHIBITED_LOG_MESSAGE,
        SyncWaitCondition.NO_INPUT_INHIBITED_LOG_MESSAGE, "");
  }

  @Override
  public void stop() {
    super.stop();
    client.removeKeyboardStatusListener(this);
  }

  @Override
  protected boolean getCurrentConditionState() {
    return !client.isInputInhibited();
  }

}
