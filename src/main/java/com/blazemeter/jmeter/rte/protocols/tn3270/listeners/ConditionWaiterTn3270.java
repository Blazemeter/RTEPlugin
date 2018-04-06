package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import java.util.concurrent.ScheduledExecutorService;

public abstract class ConditionWaiterTn3270<T extends WaitCondition> extends
    ConditionWaiter {

  public ConditionWaiterTn3270(WaitCondition condition,
      ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, stableTimeoutExecutor);
  }
}
