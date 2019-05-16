package com.blazemeter.jmeter.rte.waitsRecorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.wait.SilentWaitRecorder;
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
public class SilentWaitConditionRecorderTest extends WaitConditionRecorderIT {
  
  private final long VARIATION_OF_TIME = 1000L;
  private long stablePeriodMillis;
  private long timeoutThresholdMillis;
  private SilentWaitRecorder silentWaitRecorder;

  @Mock
  private Clock clock;

  @Mock
  private RteProtocolClient rteProtocolClientMock;


  @Before
  public void setup() {
    stablePeriodMillis = 1000;
    timeoutThresholdMillis = 10000;
    when(clock.instant()).thenReturn(Instant.now());
    silentWaitRecorder = new SilentWaitRecorder(rteProtocolClientMock, timeoutThresholdMillis, 
        stablePeriodMillis, clock);
  }

  @Test
  public void shouldReturnSilentWaitConditionWithTimeoutThresholdMillisAsValueWhenNoTerminalStateChange() {
    silentWaitRecorder.start();
    WaitCondition expectedWaitCondition= new SilentWaitCondition(
        timeoutThresholdMillis, stablePeriodMillis);
    silentWaitRecorder.stop().ifPresent(wc -> Assert.assertEquals(expectedWaitCondition, wc));
  }

  @Test
  public void shouldGetWaitConditionWithExpectedTimeoutWhenDifferenceBetweenCurrentTimeAndLastStatusChangeTimeIsZero() {
    Instant startTime = clock.instant();
    when(clock.instant()).thenReturn(startTime);
    silentWaitRecorder.start();
    silentWaitRecorder.onTerminalStateChange();
    Optional<WaitCondition> waitCondition = silentWaitRecorder.stop();
    WaitCondition expectedWaitCondition = new SilentWaitCondition(
        timeoutThresholdMillis,stablePeriodMillis);
    //As we can see when LastStatusChangeTime is not null, but still being the start time.
    // Logic from buildTimeOut is obsolete. Therefor, expected is like that ↑↑↑	
    waitCondition.ifPresent(waitCondition1 -> Assert.assertEquals(
        waitCondition1, expectedWaitCondition));
    
  }

  @Test
  public void shouldGetExpectedWaitConditionWhenTerminalStateHasChangeTwice() {
    Instant startTime = clock.instant();
    silentWaitRecorder.start();
    when(clock.instant()).thenReturn(startTime.plusMillis(VARIATION_OF_TIME));
    silentWaitRecorder.onTerminalStateChange();
    when(clock.instant()).thenReturn(startTime.plusMillis(VARIATION_OF_TIME*2));
    silentWaitRecorder.onTerminalStateChange();
    Optional<WaitCondition> waitCondition = silentWaitRecorder.stop();
    WaitCondition expected = buildExpectedWaitCondition(2);
    waitCondition.ifPresent(waitCondition1 -> Assert.assertEquals(waitCondition1, expected));

  }

  private WaitCondition buildExpectedWaitCondition(int stateChangeTimes) {
    long timeout = (VARIATION_OF_TIME*stateChangeTimes) + timeoutThresholdMillis;
    return new SilentWaitCondition(timeout, VARIATION_OF_TIME + stablePeriodMillis);
  }
  
  @Test
  public void shouldGetExpectedWaitConditionWhenTerminalStateHasChangeThrice() {
    Instant startTime = clock.instant();
    silentWaitRecorder.start();
    when(clock.instant()).thenReturn(startTime.plusMillis(VARIATION_OF_TIME));
    silentWaitRecorder.onTerminalStateChange();
    when(clock.instant()).thenReturn(startTime.plusMillis(VARIATION_OF_TIME*2));
    silentWaitRecorder.onTerminalStateChange();
    when(clock.instant()).thenReturn(startTime.plusMillis(VARIATION_OF_TIME*3));
    silentWaitRecorder.onTerminalStateChange();
    Optional<WaitCondition> waitCondition = silentWaitRecorder.stop();
    WaitCondition expected = buildExpectedWaitCondition(3);
    waitCondition.ifPresent(waitCondition1 -> Assert.assertEquals(waitCondition1, expected));
  }
  
}
