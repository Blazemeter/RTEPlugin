package com.blazemeter.jmeter.rte.recorder.emulator;

public class Segment {

  private String text;
  private int column;
  private int row;

  public Segment(String text, int column,
      int row) {
    this.text = text;
    this.column = column;
    this.row = row;
  }

  public String getText() {
    return text;
  }

  public int getColumn() {
    return column;
  }

  public int getRow() {
    return row;
  }

}
