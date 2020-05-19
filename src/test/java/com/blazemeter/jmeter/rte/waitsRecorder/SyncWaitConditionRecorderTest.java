package com.blazemeter.jmeter.rte.waitsRecorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.wait.SyncWaitRecorder;
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
public class SyncWaitConditionRecorderTest {
  private static final Optional<Boolean> FALSE = Optional.of(false);
  private static final Optional<Boolean> TRUE = Optional.of(true);
  private final long STABLE_PERIOD_MILLIS = 1000L;
  private final long TIMEOUT_THRESHOLD_MILLIS = 10000L;
  private final static long  CLOCK_STEP_MILLIS = 400L;
  private SyncWaitRecorder syncWaitRecorder;
  private Instant startTime; 
  @Mock
  private Clock clock;
  @Mock
  private RteProtocolClient rteProtocolClientMock;
  @Before
  public void setup() {
    when(clock.instant()).thenReturn(Instant.now());
    syncWaitRecorder = new SyncWaitRecorder(rteProtocolClientMock, TIMEOUT_THRESHOLD_MILLIS,
        STABLE_PERIOD_MILLIS, clock);
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(TRUE, FALSE, TRUE, FALSE);
    startTime = clock.instant();
    when(clock.instant()).thenReturn(startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 2),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 3));
  }


  @Test
  public void shouldReturnEmptyWhenMaxInputInhibitedIsBiggerThanStablePeriod() {
    Instant startTime = clock.instant();
    when(clock.instant()).thenReturn(startTime.plusMillis(CLOCK_STEP_MILLIS), 
        startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 13));
    syncWaitRecorder.start();
    syncWaitRecorder.onTerminalStateChange();
    syncWaitRecorder.onTerminalStateChange();
    assertEquals(Optional.empty(), syncWaitRecorder.stop());
  }
  
  @Test
  public void shouldReturnEmptyIfIsLastInputInhibitedWhenStop() {
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(FALSE, TRUE, FALSE, TRUE);
    syncWaitRecorder.start();
    syncWaitRecorder.onTerminalStateChange();
    syncWaitRecorder.onTerminalStateChange();
    assertEquals(Optional.empty(), syncWaitRecorder.stop());
  }
  
  @Test
  public void shouldGetExpectedIfIsNotFirstInputInhibitedWhenTerminalStateChangesOnce() {
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(FALSE, TRUE, FALSE);
    syncWaitRecorder.start();
    syncWaitRecorder.onTerminalStateChange();
    assertEquals(Optional.of(new SyncWaitCondition(
        TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD_MILLIS)), syncWaitRecorder.stop());
  }
  
  @Test
  public void shouldReturnWaitConditionWhenTerminalStateAndInputInhibitedChange() {
    syncWaitRecorder.start();
    syncWaitRecorder.onTerminalStateChange();
    syncWaitRecorder.onTerminalStateChange();
    assertEquals(Optional.of(buildExpectedWaitConditionWithNormalFlowOfInputsInhibited()),
        syncWaitRecorder.stop());

  }

  private WaitCondition buildExpectedWaitConditionWithNormalFlowOfInputsInhibited() {
    long timeout = (CLOCK_STEP_MILLIS * 2) + TIMEOUT_THRESHOLD_MILLIS;
    return new SyncWaitCondition(timeout, STABLE_PERIOD_MILLIS);
  }

  @Test
  public void shouldGetWaitConditionIgnoringNonKeyboardStatusChangesWhenStop() {
    syncWaitRecorder.start();
    syncWaitRecorder.onTerminalStateChange();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(FALSE, TRUE, FALSE);
    syncWaitRecorder.onTerminalStateChange();
    when(clock.instant()).thenReturn(startTime.plusMillis(CLOCK_STEP_MILLIS * 4));
    syncWaitRecorder.onTerminalStateChange();
    assertEquals(Optional.of(buildExpectedWaitConditionIrregularInputInhibited()),
        syncWaitRecorder.stop());

  }

  private WaitCondition buildExpectedWaitConditionIrregularInputInhibited() {
    long timeout = 3 * CLOCK_STEP_MILLIS + TIMEOUT_THRESHOLD_MILLIS;
    return new SyncWaitCondition(timeout, STABLE_PERIOD_MILLIS);
  }
}
