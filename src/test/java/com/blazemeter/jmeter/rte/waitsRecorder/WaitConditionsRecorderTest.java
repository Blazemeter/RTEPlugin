package com.blazemeter.jmeter.rte.waitsRecorder;

import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.wait.SilentWaitRecorder;
import com.blazemeter.jmeter.rte.recorder.wait.SyncWaitRecorder;
import com.blazemeter.jmeter.rte.recorder.wait.WaitConditionsRecorder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WaitConditionsRecorderTest {
  
  private final long TIMEOUT_THRESHOLD_MILLIS = 10000L;
  private final long STABLE_PERIOD_MILLIS = 1000L;
  private WaitConditionsRecorder waitConditionsRecorder;
  private final WaitCondition SYNC_WAIT_CONDITION = 
      new SyncWaitCondition(TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD_MILLIS);
  private final WaitCondition SILENT_WAIT_CONDITION = 
      new SilentWaitCondition(TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD_MILLIS);
  private Instant now;
  @Mock private SilentWaitRecorder silentWaitRecorder;
  @Mock private SyncWaitRecorder syncWaitRecorder;

  @Before
  public void setup(){ 
    waitConditionsRecorder = new WaitConditionsRecorder(silentWaitRecorder,
        syncWaitRecorder, STABLE_PERIOD_MILLIS);
    now = Instant.now();
  }
  
  @Test
  public void shouldReturnSilentWaitConditionWhenSyncWaitConditionIsNotPresent() {
    when(syncWaitRecorder.buildWaitCondition()).thenReturn(Optional.empty());
    WaitCondition silentWaitCondition = new SilentWaitCondition(
        TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD_MILLIS);
    when(silentWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(silentWaitCondition));
    List<WaitCondition> waitConditions = waitConditionsRecorder.stop();
    for (WaitCondition waitCondition : waitConditions) {
      assertEquals(silentWaitCondition, waitCondition);
    }
  }

  @Test
  public void shouldReturnSyncWaitConditionWhenDifferenceBetweenEventsLowerThanStablePeriod(){
    
    when(syncWaitRecorder.getLastStatusChangeTime()).thenReturn(Optional.of(now));
    when(silentWaitRecorder.getLastStatusChangeTime()).thenReturn(Optional.of(now.plusMillis(100)));
    when(syncWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(SYNC_WAIT_CONDITION));
    List<WaitCondition> waitConditionsExpected = new ArrayList<>();
    List<WaitCondition> waitConditions = waitConditionsRecorder.stop();
    waitConditionsExpected.add(SYNC_WAIT_CONDITION);
    assertEquals(waitConditionsExpected, waitConditions);
  }
  @Test
  public void shouldReturnSyncAndSilentWaitConditionWhenDifferenceBetweenEventsIsBiggerThanStablePeriod() {
    when(syncWaitRecorder.getLastStatusChangeTime()).thenReturn(Optional.of(now));
    when(silentWaitRecorder.getLastStatusChangeTime())
        .thenReturn(Optional.of(now.plusMillis(2000)));
    when(syncWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(SYNC_WAIT_CONDITION));
    when(silentWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(SILENT_WAIT_CONDITION));
    List<WaitCondition> waitConditions = waitConditionsRecorder.stop();
    List<WaitCondition> waitConditionsExpected = new ArrayList<>();
    waitConditionsExpected.add(SYNC_WAIT_CONDITION);
    waitConditionsExpected.add(SILENT_WAIT_CONDITION);
    assertEquals(waitConditionsExpected, waitConditions);
    
  }
}
