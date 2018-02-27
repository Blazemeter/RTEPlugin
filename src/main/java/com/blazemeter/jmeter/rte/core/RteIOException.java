package com.blazemeter.jmeter.rte.core;

public class RteIOException extends RuntimeException {

  public RteIOException(Throwable t) {
    super("Comunication error with RTEclient", t);
  }
}
