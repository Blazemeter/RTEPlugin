package com.blazemeter.jmeter.rte.core;

import java.util.Objects;

public class Position {

  public static final int UNSPECIFIED_INDEX = 0;

  private int row;
  private int column;

  // Provided for proper deserialization of sample results
  public Position() {
  }

  public Position(int row, int column) {
    this.row = row;
    this.column = column;
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
  
  public static Position getPositionFromString(String text) {
    String row = text.substring(text.indexOf('(') + 1, text.indexOf(','));
    String column = text.substring(text.indexOf(',') + 1, text.indexOf(')'));
    return new Position(Integer.parseInt(row), Integer.parseInt(column));
  }
}
