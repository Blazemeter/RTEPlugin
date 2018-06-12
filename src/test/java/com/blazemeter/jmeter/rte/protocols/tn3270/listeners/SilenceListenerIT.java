package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class SilenceListenerIT extends Tn3270ConditionWaiterIT {

  private Stopwatch waitTime;

  @Mock
  private ScreenWatcher screenWatcher;

  @Mock
  private KeyboardStatusChangedEvent keyboardStatusChangedEvent;

  @Before
  @Override
  public void setup() throws Exception {
    waitTime = Stopwatch.createStarted();
    super.setup();
  }

  @Override
  protected Tn3270ConditionWaiter<?> buildConditionWaiter() {
    return new SilenceListener(new SilentWaitCondition(TIMEOUT_MILLIS, STABLE_MILLIS),
        stableTimeoutExecutor,
        exceptionHandler,
        client);
  }

  @Test
  public void shouldUnblockAfterSilentTimeWhenNoEvents() throws Exception {
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(STABLE_MILLIS);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingKeyboardChanges()
      throws Exception {
    startPeriodicEventGenerator(buildKeyboardStateChangeGenerator());
    listener.await();
  }

  private Runnable buildKeyboardStateChangeGenerator() {
    return () -> ((SilenceListener) listener)
        .keyboardStatusChanged(keyboardStatusChangedEvent);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingCursorChanges()
      throws Exception {
    startPeriodicEventGenerator(buildCursorStateChangeGenerator());
    listener.await();
  }

  private Runnable buildCursorStateChangeGenerator() {
    return () -> ((SilenceListener) listener)
        .cursorMoved(1, 1, null);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingScreenChanges()
      throws Exception {
    startPeriodicEventGenerator(buildScreenStateChangeGenerator());
    listener.await();
  }

  private Runnable buildScreenStateChangeGenerator() {
    return () -> ((SilenceListener) listener)
        .screenChanged(screenWatcher);
  }

}
