package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.infordata.em.tn5250.XI5250Emulator;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UnlockListenerIT extends Tn5250ConditionWaiterIT {

  @Override
  protected Tn5250ConditionWaiter<?> buildConditionWaiter() {
    return new UnlockListener(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor);
  }

  @Test
  public void shouldUnblockAfterReceivingUnlockStateChange() throws Exception {
    when(emulator.getState()).thenReturn(XI5250Emulator.ST_NORMAL_UNLOCKED);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildStateChangeGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNotReceiveUnlockStateChange() throws Exception {
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingUnlockAndLockStateChanges()
      throws Exception {
    setupEverLockingAndUnlockingEmulator();
    startPeriodicEventGenerator(buildStateChangeGenerator());
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

}
