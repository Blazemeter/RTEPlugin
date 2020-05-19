package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.protocols.vt420.Vt420Client;
import java.util.concurrent.ScheduledExecutorService;
import nl.lxtreme.jvt220.terminal.ScreenChangeListener;

public class ScreenTextListener extends Vt420ConditionWaiter<TextWaitCondition> implements
    ScreenChangeListener {

  public ScreenTextListener(TextWaitCondition condition,
      Vt420Client client,
      ScheduledExecutorService stableTimeoutExecutor,
      ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addScreenChangeListener(this);
  }

  @Override
  public void screenChanged(String s) {
    updateConditionState(SCREEN_CHANGED);
  }

  @Override
  public void stop() {
    super.stop();
    client.removeScreenChangeListener(this);
  }

  @Override
  protected boolean getCurrentConditionState() {
    return condition.matchesScreen(client.getScreen());
  }
}
