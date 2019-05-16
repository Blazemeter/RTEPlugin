package com.blazemeter.jmeter.rte.waitsRecorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.wait.SyncWaitRecorder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyncWaitConditionIT extends WaitConditionRecorderIT {

  private final long stablePeriodMillis = 1000L;
  private final long timeoutThresholdMillis = 10000L;
  private long CTE = 400;
  private SyncWaitRecorder syncWaitRecorder;
  @Mock
  private Clock clock;
  @Mock
  private RteProtocolClient rteProtocolClientMock;

  @Before
  public void setup() {
    when(clock.instant()).thenReturn(Instant.now());
    syncWaitRecorder = new SyncWaitRecorder(rteProtocolClientMock, timeoutThresholdMillis, 
        stablePeriodMillis, clock);
  }

  @Test
  public void shouldReturnEmptyWhenNoChangesInTerminalStateAndChangesInInputInhibited() {
    Instant startTime = clock.instant();
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE));
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(false);
    syncWaitRecorder.start();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(true);
    Assert.assertTrue(!syncWaitRecorder.stop().isPresent());
  }

  @Test
  public void shouldReturnEmptyWhenMaxInputInhibitedIsBiggerThanStablePeriod() {
    Instant startTime = clock.instant();
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE));
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(false);
    syncWaitRecorder.start();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(true);
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE));
    syncWaitRecorder.onTerminalStateChange();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(false);
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE * 13));
    syncWaitRecorder.onTerminalStateChange();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(false);
    Assert.assertTrue(!syncWaitRecorder.stop().isPresent());
  }

  @Test
  public void shouldReturnWaitConditionWhenTerminalStateAndInputInhibitedChange() {
    //This will happen with a normal flow of inputInhibited changes.
    Instant startTime = clock.instant();
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE));
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(false);
    syncWaitRecorder.start();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(true);
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE * 2));
    syncWaitRecorder.onTerminalStateChange();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(false);
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE * 3));
    syncWaitRecorder.onTerminalStateChange();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(false);
    Optional<WaitCondition> waitCondition = syncWaitRecorder.stop();

    waitCondition.ifPresent(waitCondition1 -> Assert.assertEquals(
        buildExpectedWaitConditionWithNormalFlowOfInputsInhibited(), waitCondition1));
  }

  private WaitCondition buildExpectedWaitConditionWithNormalFlowOfInputsInhibited() {
    long timeout = (CTE * 2) + timeoutThresholdMillis;
    return new SyncWaitCondition(timeout, stablePeriodMillis);
  }

  @Test
  public void shouldGetWaitConditionWhenInputInhibitedKeepSameStateAsBefore() {
    //Here it just enter twice to onTerminalStateChange even knowing that it is called thrice,
    //cause InputInhibited have not changed once during the flow.
    Instant startTime = clock.instant();
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE));
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(false);
    syncWaitRecorder.start();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(true);
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE * 2));
    syncWaitRecorder.onTerminalStateChange();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(true);
    when(clock.instant()).thenReturn(startTime.plusMillis(CTE * 3));
    syncWaitRecorder.onTerminalStateChange();
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(false);
    syncWaitRecorder.stop().ifPresent(waitCondition1 -> Assert.assertEquals(
        buildExpectedWaitConditionIrregularInputInhibited(), waitCondition1));

  }

  private WaitCondition buildExpectedWaitConditionIrregularInputInhibited() {
    long timeout = CTE + timeoutThresholdMillis;
    return new SyncWaitCondition(timeout, stablePeriodMillis);
  }
}
