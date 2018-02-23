package com.blazemeter.jmeter.rte.core;

public class Position {

  private final int row;
  private final int column;

  public Position(int col, int row) {
    this.row = row;
    column = col;
  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return column;
  }
  
}
