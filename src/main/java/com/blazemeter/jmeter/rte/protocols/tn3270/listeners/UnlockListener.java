package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
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
  private boolean isInputInhibited;

  public UnlockListener(SyncWaitCondition condition, Tn3270Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addKeyboardStatusListener(this);
    isInputInhibited = client.isInputInhibited();
    if (!isInputInhibited) {
      LOG.debug("Start stable period since input is not inhibited");
      startStablePeriod();
    }
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent keyboardStatusChangedEvent) {
    LOG.debug("keyboardStatusChanged {}", keyboardStatusChangedEvent.toString());

    boolean wasInputInhibited = isInputInhibited;
    isInputInhibited = keyboardStatusChangedEvent.keyboardLocked;
    if (isInputInhibited != wasInputInhibited) {
      if (isInputInhibited) {
        LOG.debug("Cancel stable period since input has been inhibited");
        endStablePeriod();
      } else {
        LOG.debug("Start stable period since input is no longer inhibited");
        startStablePeriod();
      }
    }
  }

  @Override
  public void stop() {
    super.stop();
    client.removeKeyboardStatusListener(this);
  }

}
