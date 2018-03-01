package com.blazemeter.jmeter.rte.core;

import java.util.List;
import java.util.concurrent.TimeoutException;

public interface RteProtocolClient {

  boolean isConnected();

  void connect(String server, int port, TerminalType terminalType, long timeoutMillis,
      long stableTimeout) throws RteIOException, TimeoutException, InterruptedException;

  String send(List<CoordInput> input) throws InterruptedException;

  void disconnect();

}
