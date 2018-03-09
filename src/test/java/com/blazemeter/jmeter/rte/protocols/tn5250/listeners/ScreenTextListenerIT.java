package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.ExtendedEmulator;
import com.google.common.base.Stopwatch;
import java.awt.Dimension;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScreenTextListenerIT {

  private static final long TIMEOUT_MILLIS = 3000;
  private static final long STABLE_MILLIS = 1000;

  private ScheduledExecutorService stableTimeoutExecutor;
  private ExecutorService eventGeneratorExecutor;

  @Mock
  private ExtendedEmulator emulator;

  private ScreenTextListener listener;

  @Before
  public void setup() throws Exception {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    eventGeneratorExecutor = Executors.newSingleThreadExecutor();
    listener = new ScreenTextListener(
        new TextWaitCondition(new Perl5Compiler().compile("hello"), new Perl5Matcher(),
            Area.fromTopLeftBottomRight(1, 1, 1, 5), TIMEOUT_MILLIS, STABLE_MILLIS),
        stableTimeoutExecutor);
    setupScreenWithText("hello");
  }

  private void setupScreenWithText(String screen) {
    when(emulator.getString()).thenReturn(screen);
    when(emulator.getCrtSize()).thenReturn(new Dimension(screen.length(), 1));
  }

  @After
  public void teardown() {
    eventGeneratorExecutor.shutdownNow();
    stableTimeoutExecutor.shutdownNow();
  }

  @Test
  public void shouldUnblockAfterReceivingScreenWithExpectedRegexInArea() throws Exception {
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startNewPanelEventGenerator(unlockDelayMillis);
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  private void startNewPanelEventGenerator(long screenDelayMillis) {
    startEventGenerator(screenDelayMillis, buildNewPanelGenerator());
  }

  private Runnable buildNewPanelGenerator() {
    return () -> listener.newPanelReceived(
        new XI5250EmulatorEvent(XI5250EmulatorEvent.NEW_PANEL_RECEIVED, emulator));
  }

  private void startEventGenerator(long delayMillis, Runnable eventGenerator) {
    eventGeneratorExecutor.submit(() -> {
      try {
        Thread.sleep(delayMillis);
        eventGenerator.run();
      } catch (InterruptedException e) {
        //this is expected since teardown interrupts threads.
      }
    });
  }

  @Test
  public void shouldUnblockAfterReceivingStateChangeAndExceptionInEmulator() throws Exception {
    when(emulator.hasPendingError()).thenReturn(true);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startEventGenerator(unlockDelayMillis, buildStateChangeGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  private Runnable buildStateChangeGenerator() {
    return () -> listener
        .stateChanged(new XI5250EmulatorEvent(XI5250EmulatorEvent.STATE_CHANGED, emulator));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNoScreenReceivedMatchingRegexInArea()
      throws Exception {
    setupScreenWithText("  hello");
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingStateChanges()
      throws Exception {
    buildNewPanelGenerator().run();
    startPeriodicEventGenerator(buildStateChangeGenerator());
    listener.await();
  }

  private void startPeriodicEventGenerator(Runnable eventGenerator) {
    eventGeneratorExecutor.submit(() -> {
      try {
        while (true) {
          Thread.sleep(500);
          eventGenerator.run();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedExpectedScreenButKeepGettingScreens()
      throws Exception {
    startPeriodicEventGenerator(buildNewPanelGenerator());
    listener.await();
  }

}
