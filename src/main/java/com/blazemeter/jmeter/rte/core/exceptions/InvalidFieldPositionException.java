package com.blazemeter.jmeter.rte.core.exceptions;

import com.blazemeter.jmeter.rte.core.Position;

public class InvalidFieldPositionException extends IllegalArgumentException {

  public InvalidFieldPositionException(Position position, Throwable cause) {
    super("No field at row " + position.getRow() + " and column " + position.getColumn(), cause);
  }

}
