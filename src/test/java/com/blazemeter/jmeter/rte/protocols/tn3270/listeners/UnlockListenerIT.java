package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.ConditionWaiterTn5250;

public class UnlockListenerIT extends ConditionWaiterTn3270IT {

  @Override
  protected ConditionWaiterTn5250<?> buildConditionWaiter() throws Exception {
    return null;
  }
}
