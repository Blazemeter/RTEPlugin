package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listeners.ConditionWaiterIT;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class Tn3270ConditionWaiterIT  extends ConditionWaiterIT<Tn3270ConditionWaiter<?>> {

  @Mock
  protected Tn3270Client client;

}
