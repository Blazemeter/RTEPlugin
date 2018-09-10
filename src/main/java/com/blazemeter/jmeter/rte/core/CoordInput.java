package com.blazemeter.jmeter.rte.core;

public class CoordInput extends Input {

  private final Position position;

  public CoordInput(Position pos, String in) {
    super(in);
    position = pos;
  }

  public Position getPosition() {
    return position;
  }

}
