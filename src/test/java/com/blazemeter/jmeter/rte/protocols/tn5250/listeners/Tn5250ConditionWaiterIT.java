package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.listeners.ConditionWaiterIT;
import com.blazemeter.jmeter.rte.protocols.tn5250.ExtendedEmulator;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class Tn5250ConditionWaiterIT extends ConditionWaiterIT<Tn5250ConditionWaiter<?>> {

  @Mock
  protected ExtendedEmulator emulator;

  @Mock
  protected Tn5250Client client;

  @Mock
  protected ExtendedEmulator em;

  @Test
  public void shouldUnblockAfterReceivingStateChangeAndExceptionInEmulator() throws Exception {
    when(client.hasPendingError()).thenReturn(true);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildStateChangeGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  protected Runnable buildStateChangeGenerator() {
    return () -> listener
        .stateChanged(new XI5250EmulatorEvent(XI5250EmulatorEvent.STATE_CHANGED, emulator));
  }
}
