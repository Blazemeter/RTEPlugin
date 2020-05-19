package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.protocols.vt420.Vt420Client;
import java.util.concurrent.ScheduledExecutorService;
import nl.lxtreme.jvt220.terminal.ScreenChangeListener;

public class UnlockListener extends Vt420ConditionWaiter<SyncWaitCondition> implements
    ScreenChangeListener {
  
  public UnlockListener(SyncWaitCondition condition,
      Vt420Client client,
      ScheduledExecutorService stableTimeoutExecutor,
      ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addScreenChangeListener(this);
  }

  @Override
  public void stop() {
    client.removeScreenChangeListener(this);
    super.stop();
  }

  @Override
  protected boolean getCurrentConditionState() {
    return true;
  }

  @Override
  public void screenChanged(String s) {
    /*
      not a good practice to update a variable from here
      but considering that sync has the same behaviour as
      silent wait. It is the best approach here.
    */
    lastConditionState = false;
    updateConditionState(SCREEN_CHANGED);
  }

}
