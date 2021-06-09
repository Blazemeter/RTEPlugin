package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.DisconnectWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;

public class DisconnectListener extends Tn5250ConditionWaiter<DisconnectWaitCondition> {

  private boolean isDisconnected = false;

  public DisconnectListener(DisconnectWaitCondition condition,
      Tn5250Client client, ScheduledExecutorService stableTimeoutExecutor,
      ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
  }

  @Override
  protected boolean getCurrentConditionState() {
    return isDisconnected;
  }

  @Override
  public void disconnected(XI5250EmulatorEvent event) {
    isDisconnected = true;
    updateConditionState("Disconnect");
  }
}
