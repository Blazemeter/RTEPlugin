package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;

public class SilenceListenerIT extends Vt420ConditionWaiterIT {

  private Stopwatch waitTime;

  @Before
  @Override
  public void setup() throws Exception {
    waitTime = Stopwatch.createStarted();
    super.setup();
  }

  @Override
  protected Vt420ConditionWaiter<?> buildConditionWaiter() {
    return new SilenceListener(new SilentWaitCondition(TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor,
        exceptionHandler);
  }

  @Test
  public void shouldUnlockAfterSilentTimeWhenNoEvents()
      throws InterruptedException, TimeoutException, RteIOException {
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(STABLE_MILLIS);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingScreenChanges()
      throws Exception {
    startPeriodicEventGenerator(buildScreenStateChangeGenerator());
    listener.await();
  }

  private Runnable buildScreenStateChangeGenerator() {
    return () -> ((SilenceListener) listener)
        .screenChanged("");
  }
}
