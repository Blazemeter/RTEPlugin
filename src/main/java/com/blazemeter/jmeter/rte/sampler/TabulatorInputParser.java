package com.blazemeter.jmeter.rte.sampler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabulatorInputParser {

  private static final Pattern TAB_PATTERN = Pattern.compile("<TAB>");
  private static final Pattern EXTENDED_VERSION_PATTERN = Pattern
      .compile("([" + TAB_PATTERN.pattern() + "]+)");
  private static final Pattern SHORT_VERSION_PATTERN = Pattern.compile("<TAB\\*(\\d+)>");

  public static TabulatorInputRowGui parse(String offset, String input) {
    Matcher shortVersionMatcher = SHORT_VERSION_PATTERN.matcher(offset);
    Matcher extendedVersionMatcher = EXTENDED_VERSION_PATTERN.matcher(offset);

    TabulatorInputRowGui tabulatorInputRowGui = new TabulatorInputRowGui();
    tabulatorInputRowGui.setInput(input);

    if (shortVersionMatcher.matches()) {
      tabulatorInputRowGui.setOffset(shortVersionMatcher.group(1));
      return tabulatorInputRowGui;
    } else if (extendedVersionMatcher.matches()) {
      Matcher helperTabMatcher = TAB_PATTERN.matcher(extendedVersionMatcher.group(1));
      int tabOffset = 0;
      while (helperTabMatcher.find()) {
        tabOffset++;
      }
      tabulatorInputRowGui.setOffset(String.valueOf(tabOffset));
      return tabulatorInputRowGui;
    }
    throw new IllegalArgumentException(
        "Given text does not match with any type of Tabulator Input format.");
  }

}
