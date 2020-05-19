package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import com.blazemeter.jmeter.rte.core.listeners.ConditionWaiterIT;
import com.blazemeter.jmeter.rte.protocols.vt420.Vt420Client;
import com.blazemeter.jmeter.rte.protocols.vt420.listeners.Vt420ConditionWaiter;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)

public abstract class Vt420ConditionWaiterIT extends ConditionWaiterIT<Vt420ConditionWaiter<?>> {

  @Mock
  protected Vt420Client client;
}
