package com.blazemeter.jmeter.rte.core;

import java.util.Objects;

public class CoordInput extends Input {

  private final Position position;

  public CoordInput(Position pos, String input) {
    super(input);
    position = pos;
  }

  public Position getPosition() {
    return position;
  }

  @Override
  public String toString() {
    return this.position + ": " + getInput();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CoordInput that = (CoordInput) o;
    return Objects.equals(position, that.position) && Objects.equals(input, that.input);
  }

  @Override
  public int hashCode() {
    return Objects.hash(position, input);
  }
  
}
