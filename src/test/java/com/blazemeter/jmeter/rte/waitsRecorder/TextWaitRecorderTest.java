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

  private static final long STABLE_PERIOD = 1000;
  private final long TIMEOUT_THRESHOLD_MILLIS = 10000L;
  private final static long CLOCK_STEP_MILLIS = 400L;
  private final Screen EMPTY_SCREEN = new Screen(new Dimension(80, 24));
  private TextWaitRecorder textWaitRecorder;
  private Instant startTime;
  @Mock
  private Clock clock;
  @Mock
  private RteProtocolClient rteProtocolClientMock;
  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Before
  public void setup() {
    textWaitRecorder = new TextWaitRecorder(rteProtocolClientMock, TIMEOUT_THRESHOLD_MILLIS, STABLE_PERIOD, STABLE_PERIOD, clock);
    startTime = Instant.now();
  }

  @Test
  public void shouldReturnEmptyWhenTextConditionIsNotSet() throws IOException {
    when(rteProtocolClientMock.getScreen())
        .thenReturn(buildScreenFromHtmlFile("login-welcome-screen.html"));
    when(clock.instant()).thenReturn(startTime);
    textWaitRecorder.start();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.onTerminalStateChange();
    assertEquals(Optional.empty(), textWaitRecorder.stop());
  }

  @Test
  public void shouldReturnConditionWithLastTimeoutWhenWasNotStable() throws IOException {
    when(clock.instant()).thenReturn(startTime,
        startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 2),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 3));
    when(rteProtocolClientMock.getScreen())
        .thenReturn(buildScreenFromHtmlFile("login-welcome-screen.html"), EMPTY_SCREEN);
    textWaitRecorder.start();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.setWaitForTextCondition("User  \n" + "Passwo");
    assertWaitTextCondition(buildExpectedCondition(
        ChronoUnit.MILLIS.between(startTime, startTime.plusMillis(CLOCK_STEP_MILLIS)),
        "User  .*?\n.*?" + "Passwo"), textWaitRecorder.stop());
  }

  private Optional<WaitCondition> buildExpectedCondition(long timeout, String regex) {
    TextWaitCondition expected = new TextWaitCondition(JMeterUtils.getPattern(regex),
        JMeterUtils.getMatcher(),
        Area.fromTopLeftBottomRight(1, 1, 24, 80),
        timeout + TIMEOUT_THRESHOLD_MILLIS,
        RTESampler.getStableTimeout());
    return Optional.of(expected);
  }

  private void assertWaitTextCondition(Optional<WaitCondition> expected,
      Optional<WaitCondition> actual) {

    softly.assertThat(((TextWaitCondition) expected.get()).getRegex()).as("Regex")
        .isEqualTo(((TextWaitCondition) expected.get()).getRegex());
    softly.assertThat(((TextWaitCondition) expected.get()).getSearchArea()).as("SearchArea")
        .isEqualTo(((TextWaitCondition) expected.get()).getSearchArea());
    softly.assertThat(expected.get().getTimeoutMillis()).as("Timeout")
        .isEqualTo(expected.get().getTimeoutMillis());
    assertEquals(expected.isPresent(), actual.isPresent());
  }

  @Test
  public void shouldIgnoreAllScreenAfterSetCondition() throws IOException {
    when(clock.instant()).thenReturn(startTime,
        startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 2),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 3),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 4),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 10));
    when(rteProtocolClientMock.getScreen())
        .thenReturn(buildScreenFromHtmlFile("login-welcome-screen.html"),
            new Screen(new Dimension(80, 24)),
            buildScreenFromHtmlFile("login-welcome-screen.html"),
            new Screen(new Dimension(80, 24)));
    textWaitRecorder.start();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.setWaitForTextCondition("User  \n" + "Passwo");
    textWaitRecorder.onTerminalStateChange();
    assertWaitTextCondition(buildExpectedCondition(
        ChronoUnit.MILLIS.between(startTime, startTime.plusMillis(CLOCK_STEP_MILLIS)),
        "User  .*?\n.*?" + "Passwo"), textWaitRecorder.stop());
  }

  @Test
  public void shouldReturnFirstStableTimeoutWhenStableBeforeSetCondition() throws IOException {
    when(clock.instant()).thenReturn(startTime,
        startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 2),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 10),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 11),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 12));
    when(rteProtocolClientMock.getScreen())
        .thenReturn(buildScreenFromHtmlFile("login-welcome-screen.html"),
            new Screen(new Dimension(80, 24)),
            buildScreenFromHtmlFile("login-welcome-screen.html"),
            new Screen(new Dimension(80, 24)));
    textWaitRecorder.start();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.setWaitForTextCondition("User  \n" + "Passwo");
    assertWaitTextCondition(buildExpectedCondition(
        ChronoUnit.MILLIS.between(startTime, startTime.plusMillis(CLOCK_STEP_MILLIS)),
        "User  .*?\n.*?" + "Passwo"), textWaitRecorder.stop());
  }

  @Test
  public void shouldReturnLastOccurrenceTimeoutWhenIsTheOnlyStable() throws IOException {
    when(clock.instant()).thenReturn(startTime,
        startTime.plusMillis(CLOCK_STEP_MILLIS),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 2),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 10),
        startTime.plusMillis(CLOCK_STEP_MILLIS * 11));
    when(rteProtocolClientMock.getScreen())
        .thenReturn(new Screen(new Dimension(80, 24)),
            buildScreenFromHtmlFile("login-welcome-screen.html"),
            new Screen(new Dimension(80, 24)));
    textWaitRecorder.start();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.onTerminalStateChange();
    textWaitRecorder.setWaitForTextCondition("User  \n" + "Passwo");
    assertWaitTextCondition(buildExpectedCondition(
        ChronoUnit.MILLIS.between(startTime, startTime.plusMillis(CLOCK_STEP_MILLIS * 2)),
        "User  .*?\n.*?" + "Passwo"), textWaitRecorder.stop());
  }

  private URL findResource(String file) {
    return getClass().getResource(file);
  }

  private Screen buildScreenFromHtmlFile(String fileName) throws IOException {
    return Screen.fromHtml(Resources.toString(findResource(fileName), Charsets.UTF_8));
  }
}
