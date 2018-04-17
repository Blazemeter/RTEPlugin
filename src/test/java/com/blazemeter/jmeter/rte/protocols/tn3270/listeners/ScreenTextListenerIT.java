package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.google.common.base.Stopwatch;
import java.awt.Dimension;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class ScreenTextListenerIT extends Tn3270ConditionWaiterIT {

  private static final String EXPECTED_SCREEN = "hello";

  @Before
  @Override
  public void setup() throws Exception {
    setupScreenWithText("Welcome");
    super.setup();
  }

  @Mock
  private ScreenWatcher screenWatcher;

  @Mock
  private KeyboardStatusChangedEvent keyboardStatusChangedEvent;

  @Mock
  private Tn3270Client client;

  @Override
  protected Tn3270ConditionWaiter<?> buildConditionWaiter() throws Exception {
    return buildTextListener(EXPECTED_SCREEN);
  }

  private ScreenTextListener buildTextListener(String regex) throws MalformedPatternException {
    return new ScreenTextListener(
        new TextWaitCondition(new Perl5Compiler().compile(regex), new Perl5Matcher(),
            Area.fromTopLeftBottomRight(1, 1, 1, 5),
            TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor);
  }

  private void setupScreenWithText(String screen) {
    when(client.getScreen()).thenReturn(screen);
    when(client.getScreenSize()).thenReturn(new Dimension(screen.length(), 1));
  }

  @Test
  public void shouldUnblockAfterReceivingScreenWithExpectedRegexInArea() throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildScrenStateChangeGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  @Test
  public void shouldUnblockWhenScreenAlreadyContainsTextWithExpectedRegexInArea() throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    ScreenTextListener listener = buildTextListener(EXPECTED_SCREEN);
    listener.await();
  }

  protected Runnable buildKeyboardStateChangeGenerator() {
    return () -> ((ScreenTextListener) listener)
        .keyboardStatusChanged(keyboardStatusChangedEvent);
  }

  protected Runnable buildCursorStateChangeGenerator() {
    return () -> ((ScreenTextListener) listener)
        .cursorMoved(1, 1, null);
  }

  protected Runnable buildScrenStateChangeGenerator() {
    return () -> ((ScreenTextListener) listener)
        .screenChanged(screenWatcher);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNoScreenReceivedMatchingRegexInArea()
      throws Exception {
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedScreenNotMatchingRegexInArea()
      throws Exception {
    setupScreenWithText("Welcome");
    buildScrenStateChangeGenerator().run();
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingStateChanges()
      throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    buildScrenStateChangeGenerator().run();
    startPeriodicEventGenerator(buildScrenStateChangeGenerator());
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingScreens()
      throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    startPeriodicEventGenerator(buildScrenStateChangeGenerator());
    listener.await();
  }
}
