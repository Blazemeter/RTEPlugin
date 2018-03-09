package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.protocols.tn5250.ExtendedEmulator;
import com.google.common.base.Stopwatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class ConditionWaiterIT {

  protected static final long TIMEOUT_MILLIS = 3000;
  protected static final long STABLE_MILLIS = 1000;

  protected ScheduledExecutorService stableTimeoutExecutor;
  private ExecutorService eventGeneratorExecutor;

  @Mock
  protected ExtendedEmulator emulator;

  protected ConditionWaiter<?> listener;

  @Before
  public void setup() throws Exception {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    eventGeneratorExecutor = Executors.newSingleThreadExecutor();
    listener = buildConditionWaiter();
  }

  protected abstract ConditionWaiter<?> buildConditionWaiter() throws Exception;

  @After
  public void teardown() {
    eventGeneratorExecutor.shutdownNow();
    stableTimeoutExecutor.shutdownNow();
  }

  protected void startSingleEventGenerator(long delayMillis, Runnable eventGenerator) {
    eventGeneratorExecutor.submit(() -> {
      try {
        Thread.sleep(delayMillis);
        eventGenerator.run();
      } catch (InterruptedException e) {
        //this is expected since teardown interrupts threads.
      }
    });
  }

  @Test
  public void shouldUnblockAfterReceivingStateChangeAndExceptionInEmulator() throws Exception {
    when(emulator.hasPendingError()).thenReturn(true);
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

  protected void startPeriodicEventGenerator(Runnable eventGenerator) {
    eventGeneratorExecutor.submit(() -> {
      try {
        while (true) {
          Thread.sleep(500);
          eventGenerator.run();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

}
