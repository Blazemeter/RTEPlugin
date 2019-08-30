package com.blazemeter.jmeter.rte.core;

import java.awt.Dimension;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalType {

  public static final String SCREEN_SIZE_SEPARATOR = "x";
  private static final Pattern TERMINAL_TYPE_PATTERN = Pattern
      .compile("^([[A-Z]\\-\\d]+): (\\d+)x(\\d+)$");
  private String id;
  private Dimension screenSize;

  // Provided for proper deserialization of sample results
  public TerminalType() {
  }

  public TerminalType(String id, Dimension screenSize) {
    this.id = id;
    this.screenSize = screenSize;
  }

  public static TerminalType fromString(String text) {
    Matcher m = TERMINAL_TYPE_PATTERN.matcher(text);
    if (m.matches()) {
      return new TerminalType(m.group(1),
          new Dimension(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(2))));
    } else {
      throw new IllegalArgumentException("The text '" + text + "' does not match dimension format");
    }
  }

  public String getId() {
    return id;
  }

  public Dimension getScreenSize() {
    return screenSize;
  }

  @Override
  public String toString() {
    return id + ": " + screenSize.height + SCREEN_SIZE_SEPARATOR + screenSize.width;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TerminalType that = (TerminalType) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(screenSize, that.screenSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, screenSize);
  }
}
