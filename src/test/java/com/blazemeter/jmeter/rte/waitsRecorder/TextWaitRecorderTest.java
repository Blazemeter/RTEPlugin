package com.blazemeter.jmeter.rte.waitsRecorder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.wait.TextWaitRecorder;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TextWaitRecorderTest {

  public static final String SELECTED_TEXT = "User  \n" + "Passwo";
  public static final String REGEX = "User\\ \\ .*\\n.*Passwo";
  private static final long STABLE_PERIOD = 1000;
  private final static long CLOCK_STEP_MILLIS = 400L;
  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private final long TIMEOUT_THRESHOLD_MILLIS = 10000L;
  private final Screen EMPTY_SCREEN = new Screen(new Dimension(80, 24));
  private TextWaitRecorder textWaitRecorder;
  private Instant startTime;
  private Screen LOGIN_SCREEN = buildScreenFromHtmlFile("login-welcome-screen.html");
  @Mock
  private Clock clock;
  @Mock
  private RteProtocolClient rteProtocolClientMock;

  @VisibleForTesting
  public TextWaitRecorderTest() throws IOException {
  }

  @Before
  public void setup() {
    textWaitRecorder = new TextWaitRecorder(rteProtocolClientMock, TIMEOUT_THRESHOLD_MILLIS,
        STABLE_PERIOD, STABLE_PERIOD, clock);
    startTime = Instant.now();
  }

  @Test
  public void shouldReturnEmptyWhenTextConditionIsNotSet() {
    when(rteProtocolClientMock.getScreen())
        .thenReturn(LOGIN_SCREEN);
    setClockTime(0L);
    textWaitRecorder.start();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.onTerminalStateChange();
    assertEquals(Optional.empty(), textWaitRecorder.stop());
  }

  private void setClockTime(long addition) {
    when(clock.instant()).thenReturn(startTime.plusMillis(addition));
  }

  @Test
  public void shouldReturnConditionWithLastTimeoutWhenWasNotStable() {
    setClockTime(0L);
    when(rteProtocolClientMock.getScreen())
        .thenReturn(LOGIN_SCREEN, EMPTY_SCREEN, LOGIN_SCREEN);
    textWaitRecorder.start();
    setClockTime(CLOCK_STEP_MILLIS);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 2);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 3);
    textWaitRecorder.setWaitForTextCondition(SELECTED_TEXT);
    assertEquals(buildExpectedCondition(
        ChronoUnit.MILLIS.between(startTime, startTime.plusMillis(CLOCK_STEP_MILLIS)),
        REGEX), textWaitRecorder.stop());
  }

  private Optional<WaitCondition> buildExpectedCondition(long timeout, String regex) {
    TextWaitCondition expected = new TextWaitCondition(JMeterUtils.getPattern(regex),
        JMeterUtils.getMatcher(),
        Area.fromTopLeftBottomRight(1, 1, 24, 80),
        timeout + TIMEOUT_THRESHOLD_MILLIS,
        RTESampler.getStableTimeout());
    return Optional.of(expected);
  }

  @Test
  public void shouldIgnoreAllScreenAfterSetCondition() {
    setClockTime(0);
    when(rteProtocolClientMock.getScreen())
        .thenReturn(LOGIN_SCREEN, EMPTY_SCREEN, LOGIN_SCREEN, EMPTY_SCREEN);
    textWaitRecorder.start();
    setClockTime(CLOCK_STEP_MILLIS);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 2);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 3);
    textWaitRecorder.setWaitForTextCondition(SELECTED_TEXT);
    setClockTime(CLOCK_STEP_MILLIS * 4);
    textWaitRecorder.onTerminalStateChange();
    assertEquals(buildExpectedCondition(
        ChronoUnit.MILLIS.between(startTime, startTime.plusMillis(CLOCK_STEP_MILLIS)),
        REGEX), textWaitRecorder.stop());
  }

  @Test
  public void shouldReturnStartOfStableTimeoutWhenStableBeforeSetCondition() {
    setClockTime(0);
    when(rteProtocolClientMock.getScreen())
        .thenReturn(LOGIN_SCREEN, EMPTY_SCREEN, LOGIN_SCREEN, EMPTY_SCREEN);
    textWaitRecorder.start();
    setClockTime(CLOCK_STEP_MILLIS);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 2);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 10);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 11);
    textWaitRecorder.setWaitForTextCondition(SELECTED_TEXT);
    assertEquals(buildExpectedCondition(
        ChronoUnit.MILLIS.between(startTime, startTime.plusMillis(CLOCK_STEP_MILLIS)) * 10,
        REGEX), textWaitRecorder.stop());
  }

  @Test
  public void shouldReturnLastOccurrenceTimeoutWhenIsTheOnlyStable() {
    setClockTime(0);
    when(rteProtocolClientMock.getScreen())
        .thenReturn(EMPTY_SCREEN, LOGIN_SCREEN, EMPTY_SCREEN);
    textWaitRecorder.start();
    setClockTime(CLOCK_STEP_MILLIS);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 2);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 10);
    textWaitRecorder.onTerminalStateChange();
    setClockTime(CLOCK_STEP_MILLIS * 11);
    textWaitRecorder.setWaitForTextCondition(SELECTED_TEXT);
    assertEquals(buildExpectedCondition(
        ChronoUnit.MILLIS.between(startTime, startTime.plusMillis(CLOCK_STEP_MILLIS * 2)),
        REGEX), textWaitRecorder.stop());
  }

  private URL findResource(String file) {
    return getClass().getResource(file);
  }

  private Screen buildScreenFromHtmlFile(String fileName) throws IOException {
    return Screen.fromHtml(Resources.toString(findResource(fileName), Charsets.UTF_8));
  }
}
