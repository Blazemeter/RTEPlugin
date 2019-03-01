package com.blazemeter.jmeter.rte.core;

public class CoordInput extends Input {

  private final Position position;

  public CoordInput(Position pos, String input) {
    super(input);
    position = pos;
  }

  public Position getPosition() {
    return position;
  }

}
