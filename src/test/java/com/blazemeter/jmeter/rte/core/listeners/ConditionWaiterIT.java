package com.blazemeter.jmeter.rte.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.ConditionWaiter;
import com.google.common.base.Stopwatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public abstract class ConditionWaiterIT<T extends ConditionWaiter<?>> {

  protected static final long TIMEOUT_MILLIS = 3000;
  protected static final long STABLE_MILLIS = 1000;
  private static final int INITIAL_DELAY = 500;

  protected ScheduledExecutorService stableTimeoutExecutor;
  @Mock
  protected ExceptionHandler exceptionHandler;
  protected T listener;
  private ScheduledExecutorService eventGeneratorExecutor;

  @Before
  public void setup() throws Exception {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    eventGeneratorExecutor = Executors.newSingleThreadScheduledExecutor();
    when(exceptionHandler.hasPendingError()).thenReturn(false);
    listener = buildConditionWaiter();
  }

  protected abstract T buildConditionWaiter() throws Exception;

  @Test
  public void shouldUnblockAfterReceivingException() throws Exception {
    when(exceptionHandler.hasPendingError()).thenReturn(true);
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(INITIAL_DELAY, buildOnExceptionEventGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(INITIAL_DELAY);
  }

  private Runnable buildOnExceptionEventGenerator() {
    return () -> listener
        .onException(null);
  }

  @After
  public void teardown() {
    eventGeneratorExecutor.shutdownNow();
    stableTimeoutExecutor.shutdownNow();
    listener.stop();
  }

  protected void startSingleEventGenerator(long delayMillis, Runnable eventGenerator) {
    eventGeneratorExecutor.schedule(eventGenerator, delayMillis, TimeUnit.MILLISECONDS);
  }

  protected void startPeriodicEventGenerator(Runnable eventGenerator) {
    eventGeneratorExecutor
        .scheduleAtFixedRate(eventGenerator, INITIAL_DELAY, Long.divideUnsigned(STABLE_MILLIS, 2),
            TimeUnit.MILLISECONDS);
  }


}
