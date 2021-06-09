package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.wait.DisconnectWaitCondition;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

public class DisconnectListenerIT extends Vt420ConditionWaiterIT {

  @Override
  protected Vt420ConditionWaiter<?> buildConditionWaiter() throws Exception {
    return new DisconnectListener(new DisconnectWaitCondition(TIMEOUT_MILLIS), client,
        stableTimeoutExecutor, exceptionHandler);
  }

  @Test(timeout = 4000)
  public void shouldDisconnectWhenReceivingDisconnectState() throws Exception {
    long disconnectDelay = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(disconnectDelay,
        () -> ((DisconnectListener) listener).onConnectionClosed());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(disconnectDelay);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNotDisconnectSignalReceived() throws Exception {
    listener.await();
  }
}
