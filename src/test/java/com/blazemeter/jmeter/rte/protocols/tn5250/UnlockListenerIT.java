package com.blazemeter.jmeter.rte.protocols.tn5250;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.base.Stopwatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class UnlockListenerIT {

  private static final long TIMEOUT_MILLIS = 3000;
  private static final long STABLE_MILLIS = 1000;

  private ScheduledExecutorService stableTimeoutExecutor;
  private ExecutorService eventGeneratorExecutor;

  @Mock
  private XI5250Emulator emulator;

  private UnlockListener listener;

  @Before
  public void setup() {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    eventGeneratorExecutor = Executors.newSingleThreadExecutor();
    listener = new UnlockListener(TIMEOUT_MILLIS, STABLE_MILLIS, stableTimeoutExecutor);
  }

  @After
  public void teardown() {
    eventGeneratorExecutor.shutdownNow();
    stableTimeoutExecutor.shutdownNow();
  }

  @Test
  public void shouldUnblockAfterReceivingUnlockStateChange() throws Exception {
    when(emulator.getState()).thenReturn(XI5250Emulator.ST_NORMAL_UNLOCKED);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleStateChangeEventGenerator(unlockDelayMillis);
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  private void startSingleStateChangeEventGenerator(long unlockDelayMillis) {
    eventGeneratorExecutor.submit(() -> {
      try {
        Thread.sleep(unlockDelayMillis);
        listener.stateChanged(new XI5250EmulatorEvent(XI5250EmulatorEvent.STATE_CHANGED, emulator));
      } catch (InterruptedException e) {
        //this is expected since teardown interrupts threads.
      }
    });
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNotReceiveUnlockStateChange() throws Exception {
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingUnlockAndLockStateChanges()
      throws Exception {
    setupEverLockingAndUnlockingEmulator();
    startPeriodicStateChangeEventGenerator();
    listener.await();
  }

  private void setupEverLockingAndUnlockingEmulator() {
    when(emulator.getState()).thenAnswer(new Answer<Integer>() {

      private boolean locked = true;

      @Override
      public Integer answer(InvocationOnMock invocation) {
        locked = !locked;
        return locked ? XI5250Emulator.ST_NORMAL_LOCKED : XI5250Emulator.ST_NORMAL_UNLOCKED;
      }

    });
  }

  private void startPeriodicStateChangeEventGenerator() {
    eventGeneratorExecutor.submit(() -> {
      try {
        while (true) {
          Thread.sleep(500);
          listener
              .stateChanged(new XI5250EmulatorEvent(XI5250EmulatorEvent.STATE_CHANGED, emulator));
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

}
