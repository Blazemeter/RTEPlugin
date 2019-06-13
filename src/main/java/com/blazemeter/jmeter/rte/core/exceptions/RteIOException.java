package com.blazemeter.jmeter.rte.core.exceptions;

public class RteIOException extends Exception {

  public RteIOException(Throwable cause, String server) {
    super("Communication error with RTE client " + server, cause);
  }
  
}
