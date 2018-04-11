package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.protocols.tn5250.ExtendedEmulator;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.ConditionWaiterTn5250;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class ConditionWaiterTn3270IT {

  protected static final long TIMEOUT_MILLIS = 3000;
  protected static final long STABLE_MILLIS = 1000;

  protected ScheduledExecutorService stableTimeoutExecutor;
  private ScheduledExecutorService eventGeneratorExecutor;

  @Mock
  protected ExtendedEmulator emulator;

  @Mock
  protected Tn5250Client client;

  @Mock
  protected ExtendedEmulator em;

  protected ConditionWaiterTn5250<?> listener;

  @Before
  public void setup() throws Exception {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    eventGeneratorExecutor = Executors.newSingleThreadScheduledExecutor();
    listener = buildConditionWaiter();
  }

  protected abstract ConditionWaiterTn5250<?> buildConditionWaiter() throws Exception;

  @After
  public void teardown() {
    eventGeneratorExecutor.shutdownNow();
    stableTimeoutExecutor.shutdownNow();
    listener.stop();
  }

  protected void startSingleEventGenerator(long delayMillis, Runnable eventGenerator) {
    eventGeneratorExecutor.schedule(eventGenerator, delayMillis, TimeUnit.MILLISECONDS);
  }

  @Test
  public void shouldUnblockAfterReceivingStateChangeAndExceptionInEmulator() throws Exception {
/*    when(client.hasPendingError()).thenReturn(true);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildStateChangeGenerator());
    listener.await();
    
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);*/
  }

  protected Runnable buildStateChangeGenerator() {
    return () -> listener
        .stateChanged(new XI5250EmulatorEvent(XI5250EmulatorEvent.STATE_CHANGED, emulator));
  }

  protected void startPeriodicEventGenerator(Runnable eventGenerator) {
    eventGeneratorExecutor.scheduleAtFixedRate(eventGenerator, 500, 500, TimeUnit.MILLISECONDS);
  }


}
