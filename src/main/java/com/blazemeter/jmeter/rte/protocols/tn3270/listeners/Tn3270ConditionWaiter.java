package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import java.util.concurrent.ScheduledExecutorService;

public abstract class Tn3270ConditionWaiter<T extends WaitCondition> extends
    ConditionWaiter<T> {

  public Tn3270ConditionWaiter(T condition, ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, stableTimeoutExecutor);
  }
}
