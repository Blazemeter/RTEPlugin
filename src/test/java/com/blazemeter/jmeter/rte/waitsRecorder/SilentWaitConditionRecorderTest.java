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
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SilentWaitConditionRecorderTest extends WaitConditionRecorderIT {

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
    clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
    silentWaitRecorder = new SilentWaitRecorder(rteProtocolClientMock, timeoutThresholdMillis, stablePeriodMillis, clock);
  }
  
  @Test
  public void shouldReturnSilentWaitConditionWithTimeoutThresholdMillisAsValueWhenNoTerminalStateChange(){
    silentWaitRecorder.start();
    silentWaitRecorder.onTerminalStateChange();
    silentWaitRecorder.stop().ifPresent(wc -> Assert.assertEquals(buildExpectedSilentWaitConditionWithNullLastStatusChangeTime(), wc));
    }

  private SilentWaitCondition buildExpectedSilentWaitConditionWithNullLastStatusChangeTime() {
    return new SilentWaitCondition(timeoutThresholdMillis, stablePeriodMillis);
  } 
  
  @Test
  public void shouldReturnSilentWaitConditionWithExpectedTimeoutWhenDifferenceBetweenCurrentTimeAndLastStatusChangeTimeIsLowerThanStablePeriod() { 
    ///Difference between currentTime and lastStatusChangeTime is lower than stablePeriod;
    Instant startTime = clock.instant();
    silentWaitRecorder.start();
    //when(clock.instant()).thenReturn(Instant.now());
    silentWaitRecorder.onTerminalStateChange();
    //when(clock.instant()).thenReturn(Instant.now());
    Instant lastCurrentTime = clock.instant();
    silentWaitRecorder.onTerminalStateChange();
    
    Optional<WaitCondition> waitCondition = silentWaitRecorder.stop();
    waitCondition.ifPresent(waitCondition1 -> Assert.assertEquals(waitCondition1, buildExpectedWaitCondition(startTime, lastCurrentTime)));
    
  
  }
  private WaitCondition buildExpectedWaitCondition(Instant startTime, Instant currentTime) {
    long timeout = ChronoUnit.MILLIS.between(startTime, currentTime) + timeoutThresholdMillis;
    long maxPeriodMillis;
    Instant lastStatusChangeTime = currentTime; //This is redundant, keep it just to remember the logic.
    maxPeriodMillis = ChronoUnit.MILLIS.between(lastStatusChangeTime, startTime);
    return new SilentWaitCondition(timeout + maxPeriodMillis, stablePeriodMillis);
  }
  
  @Test
  public void shouldGetExpectedWaitConditionWhenTimeSinceLastStatusChangeIsBiggerThanStablePeriod() {
    Instant startTime = clock.instant();
    silentWaitRecorder.start();

    long biggerThanStablePeriod = 1000L;
    doReturn(startTime.plusMillis(100L)).when(clock.instant());
//    when(clock.instant()).thenReturn(startTime.plusMillis(100L), startTime.plusMillis(biggerThanStablePeriod));
//    
//    silentWaitRecorder.onTerminalStateChange();
//    silentWaitRecorder.onTerminalStateChange();
//    
//    
//    Optional<WaitCondition> waitCondition = silentWaitRecorder.stop();
  }
  
}
