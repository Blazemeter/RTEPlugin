package com.blazemeter.jmeter.rte.protocols.tn5250;

public class RteIOException extends RuntimeException {

  public RteIOException(Throwable t) {
    super("Comunication error with RTEclient", t);
  }
}
