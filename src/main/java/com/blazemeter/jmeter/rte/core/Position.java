package com.blazemeter.jmeter.rte.core;

import java.awt.Dimension;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Position {

  public static final int UNSPECIFIED_INDEX = 0;
  public static final Position DEFAULT_POSITION = new Position(0, 0);
  private static final Pattern POSITION_PATTERN = Pattern.compile("^\\((\\d+),(\\d+)\\)$");
  private int row;
  private int column;

  // Provided for proper deserialization of sample results
  public Position() {
  }

  public Position(int row, int column) {
    this.row = row;
    this.column = column;
  }

  public Position(Position other) {
    if (other != null) {
      this.row = other.getRow();
      this.column = other.getColumn();
    }
  }

  public static Position fromString(String text) {
    Matcher m = POSITION_PATTERN.matcher(text);
    if (m.matches()) {
      return new Position(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
    } else {
      throw new IllegalArgumentException("The text '" + text + "' does not match position format");
    }

  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return column;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Position position = (Position) o;
    return row == position.row &&
        column == position.column;
  }

  @Override
  public int hashCode() {
    return Objects.hash(row, column);
  }

  @Override
  public String toString() {
    return "(" + row + "," + column + ")";
  }

  public boolean isInside(Dimension screenDimension) {
    return (row <= screenDimension.height && row >= 1) && (
        column <= screenDimension.width
            && column >= 1);
  }

  public int compare(Object o) {
    return Comparator.comparingInt(Position::getRow)
        .thenComparing(Position::getColumn)
        .compare(this, (Position) o);
  }

  public boolean isConsecutiveWith(Position other) {
    return row == other.getRow() && column + 1 == other.getColumn();
  }
}
