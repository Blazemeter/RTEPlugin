package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import nl.lxtreme.jvt220.terminal.ScreenChangeListener;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Before;
import org.junit.Test;

public class ScreenTextListenerIT extends Vt420ConditionWaiterIT {

  private static final String EXPECTED_SCREEN = "welcome\n";

  @Before
  @Override
  public void setup() throws Exception {
    setupScreenWithText("GoodBye\n");
    super.setup();
  }

  private void setupScreenWithText(String screen) {
    when(client.getScreen()).thenReturn(Screen.valueOf(screen));
  }

  @Override
  protected Vt420ConditionWaiter<?> buildConditionWaiter() throws Exception {
    return buildTextListener(EXPECTED_SCREEN);
  }

  private ScreenTextListener buildTextListener(String regex) throws MalformedPatternException {
    return new ScreenTextListener(
        new TextWaitCondition(new Perl5Compiler().compile(regex), new Perl5Matcher(),
            Area.fromTopLeftBottomRight(1, 1, Position.UNSPECIFIED_INDEX,
                Position.UNSPECIFIED_INDEX), TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor,
        exceptionHandler);
  }

  @Test
  public void shouldUnblockAfterReceivingScreenWithExpectedRegexInArea() throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildNewScreenChange());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  private Runnable buildNewScreenChange() {
    return () -> ((ScreenChangeListener) listener).screenChanged(EXPECTED_SCREEN);
  }

  @Test
  public void shouldUnblockWhenScreenAlreadyContainsTextWithExpectedRegexInArea() throws Exception {
    ScreenTextListener listener = buildTextListener("GoodBye");
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNoScreenReceivedMatchingRegexArea()
      throws InterruptedException, TimeoutException, RteIOException {
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedScreenNotMatchingRegexArea()
      throws Exception {
    ScreenTextListener listener = buildTextListener(EXPECTED_SCREEN);
    listener.await();
  }
  
  @Test
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingStateChanges()
      throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    buildNewScreenChange().run();
    startPeriodicEventGenerator(buildNewScreenChange());
    listener.await();
  }
  
  @Test
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingScreens()
      throws Exception {
    setupScreenWithText(EXPECTED_SCREEN);
    startPeriodicEventGenerator(buildNewScreenChange());
    listener.await();
  }
}
