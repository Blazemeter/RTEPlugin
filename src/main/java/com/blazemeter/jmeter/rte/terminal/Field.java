package com.blazemeter.jmeter.rte.terminal;

import java.awt.Color;

public class Field extends Segment {

  private int length;

  public Field(String text, boolean visible, int length, int column, int row) {
    super(text, visible, column, row);
    this.length = length;
  }

  public int getLength() {
    return this.length;
  }

}
