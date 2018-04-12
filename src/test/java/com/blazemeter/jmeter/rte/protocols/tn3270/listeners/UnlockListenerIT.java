package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.ConditionWaiterTn5250;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UnlockListenerIT extends ConditionWaiterTn3270IT {

  @Override
  @Before
  public void setup() throws Exception {
    when(client.isInputInhibited()).thenReturn(true);
    super.setup();
  }

  @Override
  protected ConditionWaiterTn3270<?> buildConditionWaiter() {
    return new UnlockListener(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor,
        screen);
  }

  protected Runnable buildStateChangeGenerator(KeyboardStatusChangedEvent keyboardEvent) {
    return () -> ((UnlockListener) listener)
        .keyboardStatusChanged(keyboardEvent);
  }

  protected Runnable buildStateChangeGeneratorLockingAndUnlocking() {
    return new Runnable() {

      private boolean locked = true;

      @Override
      public void run() {
        ((UnlockListener) listener).keyboardStatusChanged(new KeyboardStatusChangedEvent(false, locked, ""));
        locked = !locked;
      }
    };
  }

  @Test
  public void shouldUnblockAfterReceivingUnlockStateChange() throws Exception {
    KeyboardStatusChangedEvent keyboardEvent = new KeyboardStatusChangedEvent(false, false, "");
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildStateChangeGenerator(keyboardEvent));
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  @Test
  public void shouldUnblockWhenAlreadyNotInputInhibited() throws Exception {
    when(client.isInputInhibited()).thenReturn(false);
    ConditionWaiterTn3270<?> listener = buildConditionWaiter();
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNotReceiveUnlockStateChange() throws Exception {
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingUnlockAndLockStateChanges()
      throws Exception {
    startPeriodicEventGenerator(buildStateChangeGeneratorLockingAndUnlocking());
    listener.await();
  }
}