package com.blazemeter.jmeter.rte.core;

public class RteIOException extends Exception {

  public RteIOException(Throwable t) {
    super("Communication error with RTE client", t);
  }
}
