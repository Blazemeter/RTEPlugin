package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.display.Screen;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class Tn3270ConditionWaiterIT {

  protected static final long TIMEOUT_MILLIS = 3000;
  protected static final long STABLE_MILLIS = 1000;

  protected ScheduledExecutorService stableTimeoutExecutor;
  private ScheduledExecutorService eventGeneratorExecutor;

  @Mock
  protected Screen screen;

  @Mock
  protected Tn3270Client client;

  protected Tn3270ConditionWaiter<?> listener;

  @Before
  public void setup() throws Exception {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    eventGeneratorExecutor = Executors.newSingleThreadScheduledExecutor();
    listener = buildConditionWaiter();
  }

  protected abstract Tn3270ConditionWaiter<?> buildConditionWaiter() throws Exception;

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
