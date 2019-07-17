package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.Dimension;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;

public class TextWaitRecorder extends WaitConditionRecorder {

  private Deque<Screenshot> screenshots = new ArrayDeque<>();
  private String regex;
  private Instant timestampWaitForText = null;
  private long stableTimeoutMillis;

  public TextWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
      long stablePeriodThresholdMillis, long stableTimeoutMillis) {
    super(rteProtocolClient, timeoutThresholdMillis, stablePeriodThresholdMillis);
    this.stableTimeoutMillis = stableTimeoutMillis;
  }

  @VisibleForTesting
  public TextWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
      long stablePeriodThresholdMillis, long stablePeriodMillis,
      Clock clock) {
    super(rteProtocolClient, timeoutThresholdMillis, stablePeriodThresholdMillis, clock);
    this.stableTimeoutMillis = stablePeriodMillis;
  }

  @Override
  public void onTerminalStateChange() {
    Screen screen = rteProtocolClient.getScreen();
    if (screenshots.isEmpty() || !screenshots.getLast().screen.equals(screen)) {
      screenshots.add(new Screenshot(screen, clock.instant()));
    }
  }

  @Override
  public Optional<WaitCondition> buildWaitCondition() {

    if (regex == null) {
      return Optional.empty();
    }

    PatternMatcher matcher = JMeterUtils.getMatcher();
    Pattern pattern = JMeterUtils.getPattern(regex);
    List<ScreenTextPeriod> screenTextStablePeriods = buildScreenTextStablePeriods(matcher, pattern);

    long timeout = ChronoUnit.MILLIS.between(startTime,
        screenTextStablePeriods.get(screenTextStablePeriods.size() - 1).timestamp);

    if (screenTextStablePeriods.isEmpty()) {
      LOG.warn(
          "The expected text appears but the screen has never been stable, "
              + "so the recorded wait for text may timeout when recorded sampler"
              + " is used. Consider using a different text that is a clear indication"
              + " that mainframe application is ready for next interaction.");
    } else if (screenTextStablePeriods.size() > 1) {
      LOG.warn(
          "The given text appears multiple times in screen with configured stable period,"
              + " so recorded wait for text may cause sampler to finish before expected."
              + " Consider using a different text that is a clear indication that mainframe"
              + " application is ready for next interaction, or consider tuning stable period"
              + " to be greater than ({})", getMaxScreenTextStablePeriod(screenTextStablePeriods));

    }

    Dimension screenSize = rteProtocolClient.getScreen().getSize();
    return Optional.of(new TextWaitCondition(pattern, matcher,
        Area.fromTopLeftBottomRight(1, 1, screenSize.height,
            screenSize.width), timeout + timeoutThresholdMillis,
        stableTimeoutMillis));
  }

  private long getMaxScreenTextStablePeriod(List<ScreenTextPeriod> stablePeriods) {
    return stablePeriods.stream()
        .limit(stablePeriods.size() - 1)
        .mapToLong(e -> e.periodMillis)
        .max()
        .getAsLong();
  }

  private List<ScreenTextPeriod> buildScreenTextStablePeriods(PatternMatcher matcher,
      Pattern pattern) {
    List<ScreenTextPeriod> textPeriods = getTextPeriods(matcher, pattern);
    List<ScreenTextPeriod> screenTextStablePeriod = textPeriods.stream()
        .filter(e -> e.periodMillis >= stableTimeoutMillis)
        .collect(Collectors.toList());
    ScreenTextPeriod lastTextPeriod = textPeriods.get(textPeriods.size() - 1);
    if (screenTextStablePeriod.isEmpty()
        || screenTextStablePeriod.get(screenTextStablePeriod.size() - 1) != lastTextPeriod) {
      screenTextStablePeriod.add(lastTextPeriod);
    }
    return screenTextStablePeriod;
  }

  private List<ScreenTextPeriod> getTextPeriods(PatternMatcher matcher, Pattern pattern) {
    List<ScreenTextPeriod> textPeriods = new ArrayList<>();
    Iterator<Screenshot> it = screenshots.iterator();
    while (it.hasNext()) {
      Screenshot current = it.next();
      if (!current.timestamp.isBefore(timestampWaitForText)) {
        return textPeriods;
      }
      if (matcher.contains(current.screen.getText(), pattern)) {
        Screenshot next = current;
        while (it.hasNext() && matcher.contains(next.screen.getText(), pattern)) {
          next = it.next();
        }
        textPeriods.add(new ScreenTextPeriod(current.timestamp,
            ChronoUnit.MILLIS.between(current.timestamp, next.timestamp)));
      }
    }
    return textPeriods;
  }

  public void start() {
    super.start();
    screenshots.clear();
    regex = null;
    timestampWaitForText = null;
  }

  public void setWaitForTextCondition(String text) {
    regex = text.replace("\n", ".*\\n.*");
    timestampWaitForText = clock.instant();
  }

  private static class ScreenTextPeriod {

    private Instant timestamp;
    private long periodMillis;

    private ScreenTextPeriod(Instant timestamp, long periodMillis) {
      this.timestamp = timestamp;
      this.periodMillis = periodMillis;
    }

  }

  private static class Screenshot {

    private final Screen screen;
    private final Instant timestamp;

    private Screenshot(Screen screen, Instant timestamp) {
      this.screen = screen;
      this.timestamp = timestamp;
    }

  }
}
