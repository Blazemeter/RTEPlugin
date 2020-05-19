package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.vt420.Vt420Client;
import java.util.concurrent.ScheduledExecutorService;

public abstract class Vt420ConditionWaiter<T extends WaitCondition> extends ConditionWaiter<T> {

  protected static final String SCREEN_CHANGED = "screenChanged";
  protected final Vt420Client client;

  public Vt420ConditionWaiter(T condition,
      Vt420Client client, ScheduledExecutorService stableTimeoutExecutor,
      ExceptionHandler exceptionHandler) {
    super(condition, stableTimeoutExecutor, exceptionHandler);
    this.client = client;
    initialVerificationOfCondition();
  }
}
