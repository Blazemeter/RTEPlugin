package com.blazemeter.jmeter.rte.core;

import java.net.SocketException;

public class ConnectionClosedException extends SocketException {

  public ConnectionClosedException() {
    super("Connection closed by remote end");
  }

}
