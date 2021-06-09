package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.DisconnectWaitCondition;
import com.blazemeter.jmeter.rte.protocols.vt420.Vt420Client;
import java.util.concurrent.ScheduledExecutorService;
import nl.lxtreme.jvt220.terminal.ConnectionListener;

public class DisconnectListener extends Vt420ConditionWaiter<DisconnectWaitCondition> implements
    ConnectionListener {

  private boolean disconnected;

  public DisconnectListener(DisconnectWaitCondition condition,
      Vt420Client client,
      ScheduledExecutorService stableTimeoutExecutor,
      ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addConnectionListener(this);
  }

  @Override
  protected boolean getCurrentConditionState() {
    return disconnected;
  }

  @Override
  public void onConnectionClosed() {
    disconnected = true;
    updateConditionState("Disconnect");
  }

  @Override
  public void stop() {
    super.stop();
    client.removeConnectionListener(this);
  }

  @Override
  public void onConnection() {

  }
}
