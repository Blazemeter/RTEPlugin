package com.blazemeter.jmeter.rte.core.exceptions;

import java.net.SocketException;

public class ConnectionClosedException extends SocketException {

  public ConnectionClosedException() {
    super("Connection closed by remote end");
  }
  
  public ConnectionClosedException(String cause) {
    super(cause);
  }

}
