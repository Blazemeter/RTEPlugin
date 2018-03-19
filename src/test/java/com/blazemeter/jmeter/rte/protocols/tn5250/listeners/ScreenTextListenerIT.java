package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.google.common.base.Stopwatch;
import java.awt.Dimension;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Test;

public class ScreenTextListenerIT extends ConditionWaiterIT {

  @Override
  protected ConditionWaiter<?> buildConditionWaiter() throws Exception {
    String screenText = "hello";
    setupScreenWithText(screenText);
    return buildTextListener(screenText);
  }

  private ScreenTextListener buildTextListener(String regex) throws MalformedPatternException {
    return new ScreenTextListener(
        new TextWaitCondition(new Perl5Compiler().compile(regex), new Perl5Matcher(),
            Area.fromTopLeftBottomRight(1, 1, 1, 5), TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor);
  }

  private void setupScreenWithText(String screen) {
    when(client.getScreen()).thenReturn(screen);
    when(client.getScreenSize()).thenReturn(new Dimension(screen.length(), 1));
  }

  @Test
  public void shouldUnblockAfterReceivingScreenWithExpectedRegexInArea() throws Exception {
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startNewPanelEventGenerator(unlockDelayMillis);
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  @Test
  public void shouldUnblockWhenScreenAlreadyContainsTextWithExpectedRegexInArea() throws Exception {
    listener.await();
  }

  private void startNewPanelEventGenerator(long screenDelayMillis) {
    startSingleEventGenerator(screenDelayMillis, buildNewPanelGenerator());
  }

  private Runnable buildNewPanelGenerator() {
    return () -> listener.newPanelReceived(
        new XI5250EmulatorEvent(XI5250EmulatorEvent.NEW_PANEL_RECEIVED, emulator));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNoScreenReceivedMatchingRegexInArea()
      throws Exception {
    ScreenTextListener listener = buildTextListener("Hi");
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingStateChanges()
      throws Exception {
    buildNewPanelGenerator().run();
    startPeriodicEventGenerator(buildStateChangeGenerator());
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingScreens()
      throws Exception {
    startPeriodicEventGenerator(buildNewPanelGenerator());
    listener.await();
  }

}
