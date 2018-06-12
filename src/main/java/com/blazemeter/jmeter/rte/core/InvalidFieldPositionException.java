package com.blazemeter.jmeter.rte.core;

public class InvalidFieldPositionException extends IllegalArgumentException {

  public InvalidFieldPositionException(Position position) {
    this(position, null);
  }

  public InvalidFieldPositionException(Position position, Throwable cause) {
    super("No field at row " + position.getRow() + " and column " + position.getColumn(), cause);
  }

}
