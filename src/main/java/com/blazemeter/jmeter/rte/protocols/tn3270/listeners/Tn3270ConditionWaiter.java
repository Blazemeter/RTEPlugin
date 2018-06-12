package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.bytezone.dm3270.TerminalClient;
import java.util.concurrent.ScheduledExecutorService;

public abstract class Tn3270ConditionWaiter<T extends WaitCondition> extends
    ConditionWaiter<T> {

  protected TerminalClient client;

  public Tn3270ConditionWaiter(T condition, ScheduledExecutorService stableTimeoutExecutor,
      ExceptionHandler exceptionHandler, TerminalClient client) {
    super(condition, stableTimeoutExecutor, exceptionHandler);
    this.client = client;
  }
}
