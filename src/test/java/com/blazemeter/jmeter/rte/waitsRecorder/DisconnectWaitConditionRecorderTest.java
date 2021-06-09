package com.blazemeter.jmeter.rte.waitsRecorder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.DisconnectWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.wait.DisconnectWaitRecorder;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DisconnectWaitConditionRecorderTest {

  private final static long CLOCK_STEP_MILLIS = 1000L;
  private final long TIMEOUT_THRESHOLD_MILLIS = 10000L;

  @Mock
  public Clock clock;

  @Mock
  private RteProtocolClient client;
  private DisconnectWaitRecorder disconnectWaitConditionRecorder;
  private Instant startTime;

  @Before
  public void setup() {
    when(clock.instant()).thenReturn(Instant.now());
    long STABLE_PERIOD_MILLIS = 1000L;
    disconnectWaitConditionRecorder = new DisconnectWaitRecorder(client, TIMEOUT_THRESHOLD_MILLIS,
        STABLE_PERIOD_MILLIS, clock);
    startTime = clock.instant();
  }

  @Test
  public void shouldReturnDisconnectWaitConditionWhenServerDisconnection() {
    disconnectWaitConditionRecorder.start();
    when(clock.instant()).thenReturn(startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 2));
    when(client.isServerDisconnected()).thenReturn(true);
    disconnectWaitConditionRecorder.onTerminalStateChange();
    disconnectWaitConditionRecorder.onTerminalStateChange();
    WaitCondition expected = buildExpectedWaitCondition();
    assertEquals(Optional.of(expected), disconnectWaitConditionRecorder.stop());
  }

  private WaitCondition buildExpectedWaitCondition() {
    return new DisconnectWaitCondition((CLOCK_STEP_MILLIS * 2) + TIMEOUT_THRESHOLD_MILLIS);
  }

  @Test
  public void shouldReturnEmptyWhenServerDoesNotDisconnect() {
    disconnectWaitConditionRecorder.start();
    when(clock.instant()).thenReturn(startTime.plusMillis(CLOCK_STEP_MILLIS));
    when(client.isServerDisconnected()).thenReturn(false);
    disconnectWaitConditionRecorder.onTerminalStateChange();
    assertEquals(Optional.empty(), disconnectWaitConditionRecorder.buildWaitCondition());
  }

}
