package com.blazemeter.jmeter.rte.core.exceptions;

public class InvalidFieldLabelException extends IllegalArgumentException {

  public InvalidFieldLabelException(String label, Throwable cause) {
    super("No field with label '" + label + "'", cause);
  }

}
