package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.wait.DisconnectWaitCondition;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.junit.Test;
import org.mockito.Mock;

public class DisconnectListenerIT extends Tn5250ConditionWaiterIT {

  @Mock
  XI5250EmulatorEvent eventMock;

  @Override
  protected Tn5250ConditionWaiter<?> buildConditionWaiter() {
    return new DisconnectListener(new DisconnectWaitCondition(TIMEOUT_MILLIS),
        client,
        stableTimeoutExecutor,
        exceptionHandler);

  }

  @Test(timeout = 4000)
  public void shouldDisconnectWhenReceivingDisconnectState() throws Exception {
    long disconnectDelay = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(disconnectDelay, () -> (listener).disconnected(eventMock));
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(disconnectDelay);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNotDisconnectSignalReceived() throws Exception {
    listener.await();
  }

}
