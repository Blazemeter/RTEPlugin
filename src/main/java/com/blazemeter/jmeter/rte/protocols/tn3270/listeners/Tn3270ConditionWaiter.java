package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import java.util.concurrent.ScheduledExecutorService;

public abstract class Tn3270ConditionWaiter<T extends WaitCondition> extends
    ConditionWaiter<T> {

  protected final Tn3270Client client;

  public Tn3270ConditionWaiter(T condition, Tn3270Client client,
      ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, stableTimeoutExecutor);
    this.client = client;
  }

  public void stateChanged(String event) {
    if (client.hasPendingError()) {
      cancelWait();
    }
  }

}
