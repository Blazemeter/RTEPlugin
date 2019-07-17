package com.blazemeter.jmeter.rte.waitsRecorder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.wait.SilentWaitRecorder;
import com.blazemeter.jmeter.rte.recorder.wait.SyncWaitRecorder;
import com.blazemeter.jmeter.rte.recorder.wait.TextWaitRecorder;
import com.blazemeter.jmeter.rte.recorder.wait.WaitConditionsRecorder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WaitConditionsRecorderTest {

  private final long TIMEOUT_THRESHOLD_MILLIS = 10000L;
  private final long STABLE_PERIOD_MILLIS = 1000L;
  private final WaitCondition SYNC_WAIT_CONDITION =
      new SyncWaitCondition(TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD_MILLIS);
  private final WaitCondition SILENT_WAIT_CONDITION =
      new SilentWaitCondition(TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD_MILLIS);
  private WaitConditionsRecorder waitConditionsRecorder;
  private Instant now;
  @Mock
  private SilentWaitRecorder silentWaitRecorder;
  @Mock
  private SyncWaitRecorder syncWaitRecorder;
  @Mock
  private TextWaitRecorder textWaitRecorder;
  @Mock
  private PatternMatcher matcher;
  @Mock
  private Pattern regex;
  @Mock
  private Area area;
  private final WaitCondition TEXT_WAIT_CONDITION = new TextWaitCondition(regex, matcher, area,
      TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD_MILLIS);

  @Before
  public void setup() {
    waitConditionsRecorder = new WaitConditionsRecorder(silentWaitRecorder,
        syncWaitRecorder, textWaitRecorder, STABLE_PERIOD_MILLIS);
    now = Instant.now();
  }

  @Test
  public void shouldReturnSilentWaitConditionWhenSyncAndTextWaitConditionsAreNotPresent() {
    when(silentWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(SILENT_WAIT_CONDITION));
    List<WaitCondition> waitConditions = waitConditionsRecorder.stop();
    assertEquals(Collections.singletonList(SILENT_WAIT_CONDITION), waitConditions);
  }

  @Test
  public void shouldReturnSyncWaitConditionWhenDifferenceBetweenEventsLowerThanStablePeriod() {
    when(syncWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(SYNC_WAIT_CONDITION));
    when(syncWaitRecorder.getLastStatusChangeTime()).thenReturn(Optional.of(now));
    when(silentWaitRecorder.getLastStatusChangeTime()).thenReturn(
        Optional.of(now.plusMillis(STABLE_PERIOD_MILLIS - 100)));
    List<WaitCondition> waitConditions = waitConditionsRecorder.stop();
    assertEquals(Collections.singletonList(SYNC_WAIT_CONDITION), waitConditions);
  }

  @Test
  public void shouldReturnSyncAndSilentWaitConditionWhenDifferenceBetweenEventsIsBiggerThanStablePeriod() {
    when(syncWaitRecorder.getLastStatusChangeTime()).thenReturn(Optional.of(now));
    when(silentWaitRecorder.getLastStatusChangeTime())
        .thenReturn(Optional.of(now.plusMillis(STABLE_PERIOD_MILLIS + 100)));
    when(syncWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(SYNC_WAIT_CONDITION));
    when(silentWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(SILENT_WAIT_CONDITION));
    List<WaitCondition> waitConditions = waitConditionsRecorder.stop();
    List<WaitCondition> expectedWaitConditions = new ArrayList<>();
    expectedWaitConditions.add(SYNC_WAIT_CONDITION);
    expectedWaitConditions.add(SILENT_WAIT_CONDITION);
    assertEquals(expectedWaitConditions, waitConditions);
  }

  @Test
  public void shouldReturnSyncAndTextWhenDifferenceBetweenEventsIsLowerThanStablePeriod() {
    when(syncWaitRecorder.getLastStatusChangeTime()).thenReturn(Optional.of(now));
    when(silentWaitRecorder.getLastStatusChangeTime())
        .thenReturn(Optional.of(now.plusMillis(STABLE_PERIOD_MILLIS + 100)));
    when(syncWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(SYNC_WAIT_CONDITION));
    when(textWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(TEXT_WAIT_CONDITION));
    when(silentWaitRecorder.buildWaitCondition()).thenReturn(Optional.of(SILENT_WAIT_CONDITION));
    List<WaitCondition> waitConditions = waitConditionsRecorder.stop();
    List<WaitCondition> expectedWaitConditions = new ArrayList<>();
    expectedWaitConditions.add(SYNC_WAIT_CONDITION);
    expectedWaitConditions.add(TEXT_WAIT_CONDITION);
    assertEquals(expectedWaitConditions, waitConditions);
  }
}
