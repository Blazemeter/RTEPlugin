package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.DisconnectWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.ConnectionListener;
import java.util.concurrent.ScheduledExecutorService;

public class DisconnectListener extends Tn3270ConditionWaiter<DisconnectWaitCondition> implements
    ConnectionListener {

  private boolean disconnected;

  public DisconnectListener(
      DisconnectWaitCondition condition, Tn3270Client client,
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
  public void stop() {
    super.stop();
    client.removeConnectionListener(this);
  }

  @Override
  public void onConnection() {
    
  }

  @Override
  public void onException(Exception e) {

  }

  @Override
  public void onConnectionClosed() {
    disconnected = true;
    updateConditionState("Disconnect");
  }
}
