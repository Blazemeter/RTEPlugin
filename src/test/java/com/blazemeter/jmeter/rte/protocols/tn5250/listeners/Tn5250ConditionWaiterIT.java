package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listeners.ConditionWaiterIT;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class Tn5250ConditionWaiterIT extends ConditionWaiterIT<Tn5250ConditionWaiter<?>> {

  @Mock
  protected Tn5250Client client;

  @Mock
  protected XI5250Emulator emulator;

  protected Runnable buildStateChangeGenerator() {
    return () -> listener
        .stateChanged(new XI5250EmulatorEvent(XI5250EmulatorEvent.STATE_CHANGED, emulator));
  }
}
