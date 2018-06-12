package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.google.common.base.Stopwatch;
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

  @Mock
  private ScreenWatcher screenWatcher;

  @Before
  @Override
  public void setup() throws Exception {
    setupScreenWithText("Welcome");
    super.setup();
  }

  private void setupScreenWithText(String screen) {
    when(client.getScreenText()).thenReturn(screen);
    when(client.getScreenDimensions()).thenReturn(new ScreenDimensions(1, screen.length()));
  }

  @Override
  protected Tn3270ConditionWaiter<?> buildConditionWaiter() throws Exception {
    return buildTextListener(EXPECTED_SCREEN);
  }

  private ScreenTextListener buildTextListener(String regex) throws MalformedPatternException {
    return new ScreenTextListener(
        new TextWaitCondition(new Perl5Compiler().compile(regex), new Perl5Matcher(),
            Area.fromTopLeftBottomRight(1, 1, 1, 5),
            TIMEOUT_MILLIS, STABLE_MILLIS),
        stableTimeoutExecutor,
        exceptionHandler,
        client);
  }

  @Test
  public void shouldUnblockAfterReceivingScreenWithExpectedRegexInArea() throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildScreenStateChangeGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  @Test
  public void shouldUnblockWhenScreenAlreadyContainsTextWithExpectedRegexInArea() throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    ScreenTextListener listener = buildTextListener(EXPECTED_SCREEN);
    listener.await();
  }

  private Runnable buildScreenStateChangeGenerator() {
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
    buildScreenStateChangeGenerator().run();
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingStateChanges()
      throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    buildScreenStateChangeGenerator().run();
    startPeriodicEventGenerator(buildScreenStateChangeGenerator());
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingScreens()
      throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    startPeriodicEventGenerator(buildScreenStateChangeGenerator());
    listener.await();
  }

}
