package com.blazemeter.jmeter.rte.waitsRecorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.wait.SilentWaitRecorder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SilentWaitConditionRecorderTest {
  
  private final static long CLOCK_STEP_MILLIS = 1000L;
  private final long STABLE_PERIOD_MILLIS = 1000L;
  private final long TIMEOUT_THRESHOLD_MILLIS = 10000L;
  private SilentWaitRecorder silentWaitRecorder;
  private Instant startTime;
  @Mock
  private Clock clock;

  @Mock
  private RteProtocolClient rteProtocolClientMock;
  
  @Before
  public void setup() {
    when(clock.instant()).thenReturn(Instant.now());
    silentWaitRecorder = new SilentWaitRecorder(rteProtocolClientMock, TIMEOUT_THRESHOLD_MILLIS,
        STABLE_PERIOD_MILLIS, clock);
    startTime = clock.instant();
  }

  @Test
  public void shouldReturnSilentWaitConditionWithTimeoutSetToThresholdMillisWhenNoTerminalStateChange() {
    silentWaitRecorder.start();
    WaitCondition expectedWaitCondition= new SilentWaitCondition(
        TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD_MILLIS);
    assertEquals(Optional.of(expectedWaitCondition), silentWaitRecorder.stop());
  }

  @Test
  public void shouldGetExpectedWaitConditionWhenTerminalStateHasChangeTwice() {
    silentWaitRecorder.start();
    when(clock.instant()).thenReturn(startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS *2));
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.onTerminalStateChange();
    WaitCondition expected = buildExpectedWaitCondition(2);
    assertEquals(Optional.of(expected), silentWaitRecorder.stop());


  }

  private WaitCondition buildExpectedWaitCondition(int stateChangeTimes) {
    long timeout = (CLOCK_STEP_MILLIS *stateChangeTimes) + TIMEOUT_THRESHOLD_MILLIS;
    return new SilentWaitCondition(
        timeout, CLOCK_STEP_MILLIS + STABLE_PERIOD_MILLIS);
  }
  
  @Test
  public void shouldGetMaximumSilentPeriodPlusThresholdWaitConditionWhenStateChangesMultipleTimes() {
    silentWaitRecorder.start();
    when(clock.instant()).thenReturn(startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS *2),
        startTime.plusMillis(CLOCK_STEP_MILLIS *3));
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.onTerminalStateChange();
    WaitCondition expected = buildExpectedWaitCondition(3);
    assertEquals(Optional.of(expected), silentWaitRecorder.stop());
  }
  
  @Test
  public void shouldGetExpectedWaitWhenStartSecondRecordingWithNoTerminalStateChanges() {
    silentWaitRecorder.start();
    when(clock.instant()).thenReturn(startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS *2),
        startTime.plusMillis(CLOCK_STEP_MILLIS *3),
        startTime.plusMillis(CLOCK_STEP_MILLIS*4));
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.stop();
    silentWaitRecorder.start();
    assertEquals(Optional.of( new SilentWaitCondition(
        TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD_MILLIS)), silentWaitRecorder.stop());
    
  }
  @Test
  public void shouldGetExpectedWaitWhenWhenStartSecondRecordingWithTerminalStateChanges() {
    silentWaitRecorder.start();
    when(clock.instant()).thenReturn(startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS *2),
        startTime.plusMillis(CLOCK_STEP_MILLIS *3),
        startTime.plusMillis(CLOCK_STEP_MILLIS *4),
        startTime.plusMillis(CLOCK_STEP_MILLIS *5),
        startTime.plusMillis(CLOCK_STEP_MILLIS *6));
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.stop();
    silentWaitRecorder.start();
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.onTerminalStateChange();
    WaitCondition expected = buildExpectedWaitCondition(2);
    assertEquals(Optional.of(expected), silentWaitRecorder.stop());
  }
  
  
}
