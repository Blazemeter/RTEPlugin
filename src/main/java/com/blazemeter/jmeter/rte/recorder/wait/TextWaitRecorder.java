package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.helger.commons.annotation.VisibleForTesting;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;

public class TextWaitRecorder extends WaitConditionRecorder {

  private LinkedList<Screenshot> screenshots = new LinkedList<>();

  private String regex = null;
  private Instant timestampWaitForText = null;

  public TextWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis) {
    super(rteProtocolClient, timeoutThresholdMillis, 0);
  }

  @VisibleForTesting
  public TextWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
      Clock clock) {
    super(rteProtocolClient, timeoutThresholdMillis, 0, clock);
  }

  @Override
  public void onTerminalStateChange() {
    Screen screen = rteProtocolClient.getScreen();
    if (screenshots.isEmpty() || !screenshots.get(screenshots.size() - 1)
        .equals(screen)) {
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
    Screenshot firstStable = findFirstStableOccurrence(matcher, pattern);
    Screenshot last = findLastOccurrence(matcher, pattern);
    long timeout = ChronoUnit.MILLIS.between(startTime, last.timestamp);
    if (firstStable == null) {
      LOG.warn(
          "The expected text appears but the screen has never been stable, "
              + "so the sampler may throw a timeout.");
    } else if (!firstStable.equals(last)) {
      LOG.warn(
          "The expected text has appeared before then the user has indicated, "
              + "so the sampler may finish before the expected.");
      timeout = ChronoUnit.MILLIS.between(startTime, firstStable.timestamp);
    }
    return Optional.of(new TextWaitCondition(pattern, matcher,
        Area.fromTopLeftBottomRight(1, 1, rteProtocolClient.getScreen().getSize().height,
            rteProtocolClient.getScreen().getSize().width), timeout + timeoutThresholdMillis,
        RTESampler.getStableTimeout()));
  }

  private Screenshot findFirstStableOccurrence(PatternMatcher matcher, Pattern pattern) {
    Iterator<Screenshot> it = screenshots.iterator();
    while (it.hasNext()) {
      Screenshot current = it.next();
      if (current.timestamp.isBefore(timestampWaitForText)) {
        if (matcher.contains(current.screen.getText(), pattern)) {
          Screenshot next = current;
          while (it.hasNext() && matcher.contains(next.screen.getText(), pattern)) {
            next = it.next();
          }
          long period = ChronoUnit.MILLIS.between(current.timestamp, next.timestamp);
          if (period > RTESampler.getStableTimeout()) {
            return current;
          }
        }
      }
    }
    return null;
  }

  private Screenshot findLastOccurrence(PatternMatcher matcher, Pattern pattern) {
    Iterator<Screenshot> it = screenshots.descendingIterator();
    while (it.hasNext()) {
      Screenshot screenshot = it.next();
      if (matcher.contains(screenshot.screen.getText(), pattern) && screenshot.timestamp
          .isBefore(timestampWaitForText)) {
        return screenshot;
      }
    }
    return null;
  }

  public void setWaitForTextCondition(String text) {
    regex = text.replace("\n", ".*?\\n.*?");
    timestampWaitForText = clock.instant();
  }

  private static class Screenshot {

    private final Screen screen;
    private final Instant timestamp;

    Screenshot(Screen screen, Instant timestamp) {
      this.screen = screen;
      this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Screenshot that = (Screenshot) o;
      return Objects.equals(screen, that.screen) &&
          Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
      return Objects.hash(screen, timestamp);
    }
  }
}
