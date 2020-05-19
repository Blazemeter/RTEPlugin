package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.protocols.vt420.Vt420Client;
import java.util.concurrent.ScheduledExecutorService;
import nl.lxtreme.jvt220.terminal.ScreenChangeListener;

public class SilenceListener extends Vt420ConditionWaiter<SilentWaitCondition> implements
    ScreenChangeListener {

  public SilenceListener(SilentWaitCondition condition,
      Vt420Client client,
      ScheduledExecutorService stableTimeoutExecutor,
      ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addScreenChangeListener(this);
  }

  @Override
  public void screenChanged(String s) {
    /*  
      we are updating over here because 
      silent does not really have a 
      condition. Then always when some event
      arrives we need to startStablePeriod again. 
    */
    lastConditionState = false;
    updateConditionState(SCREEN_CHANGED);
  }

  @Override
  public void stop() {
    super.stop();
    client.removeScreenChangeListener(this);
  }

  @Override
  protected boolean getCurrentConditionState() {
    return true;
  }
}
