package com.blazemeter.jmeter.rte.emulator;

public class Field extends Segment {

  private int length;

  public Field(String text, int length, int column, int row) {
    super(text, column, row);
    this.length = length;
  }

  public int getLength() {
    return this.length;
  }

}
