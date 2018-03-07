package com.blazemeter.jmeter.rte.core.wait;

import java.awt.Dimension;
import java.util.Objects;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;

/**
 * {@link WaitCondition} to wait for certain text to be in terminal screen.
 */
public class TextWaitCondition extends WaitCondition {

  private final Pattern regex;
  private final PatternMatcher matcher;
  private final Area searchArea;

  public TextWaitCondition(Pattern regex, PatternMatcher matcher, Area searchArea,
      long timeoutMillis,
      long stableTimeoutMillis) {
    super(timeoutMillis, stableTimeoutMillis);
    this.regex = regex;
    this.matcher = matcher;
    this.searchArea = searchArea;
  }

  @Override
  public String getDescription() {
    return "emulator screen area " + searchArea + " to contain " + regex;
  }

  public boolean matchesScreen(String screen, Dimension screenSize) {
    String screenArea = extractScreenArea(searchArea, screen, screenSize);
    return matcher.contains(screenArea, regex);
  }

  private String extractScreenArea(Area searchArea, String screen, Dimension screenSize) {
    StringBuilder builder = new StringBuilder();
    int top = Math.max(1, searchArea.getTop());
    int left = Math.max(1, searchArea.getLeft());
    int bottom = Math.min(searchArea.getBottom() <= 0 ? screenSize.height : searchArea.getBottom(),
        screenSize.height);
    int right = Math.min(searchArea.getRight() <= 0 ? screenSize.width : searchArea.getRight(),
        screenSize.width);
    for (int i = top; i <= bottom; i++) {
      int rowStart = (i - 1) * screenSize.width;
      builder.append(screen.substring(rowStart + left - 1, rowStart + right));
      builder.append("\n");
    }
    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    TextWaitCondition that = (TextWaitCondition) o;
    return super.equals(o) &&
        Objects.equals(regex, that.regex) &&
        Objects.equals(matcher, that.matcher) &&
        Objects.equals(searchArea, that.searchArea);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), regex, matcher, searchArea);
  }

  @Override
  public String toString() {
    return "TextWaitCondition{" +
        "regex=" + regex +
        ", matcher=" + matcher +
        ", searchArea=" + searchArea +
        ", timeoutMillis=" + timeoutMillis +
        ", stableTimeoutMillis=" + stableTimeoutMillis +
        '}';
  }
}
