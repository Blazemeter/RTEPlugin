package com.blazemeter.jmeter.rte.terminal;

public class Segment {

  private String text;
  private boolean visible;
  private int column;
  private int row;

  public Segment(String text, boolean visible, int column,
      int row) {
    this.text = text;
    this.visible = visible;
    this.column = column;
    this.row = row;
  }

  public String getText() {
    return text;
  }

  public boolean isVisible() {
    return visible;
  }

  public int getColumn() {
    return column;
  }

  public int getRow() {
    return row;
  }
}
