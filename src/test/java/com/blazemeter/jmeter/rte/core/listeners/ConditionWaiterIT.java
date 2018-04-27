package com.blazemeter.jmeter.rte.core.listeners;

import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;

public abstract class ConditionWaiterIT<T extends ConditionWaiter<?>> {

  protected static final long TIMEOUT_MILLIS = 3000;
  protected static final long STABLE_MILLIS = 1000;

  protected ScheduledExecutorService stableTimeoutExecutor;
  private ScheduledExecutorService eventGeneratorExecutor;

  protected T listener;

  @Before
  public void setup() throws Exception {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    eventGeneratorExecutor = Executors.newSingleThreadScheduledExecutor();
    listener = buildConditionWaiter();
  }

  protected abstract T buildConditionWaiter() throws Exception;

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
    eventGeneratorExecutor.scheduleAtFixedRate(eventGenerator, 500, 500, TimeUnit.MILLISECONDS);
  }


}
