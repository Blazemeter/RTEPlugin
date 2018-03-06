package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.protocols.tn5250.SSLData;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface RteProtocolClient {

  boolean isConnected();

  void connect(String server, int port, SSLData sslData,
               TerminalType terminalType, long timeoutMillis, long stableTimeout)
      throws RteIOException, TimeoutException, InterruptedException;

  String send(List<CoordInput> input, Action action) throws InterruptedException;

  void disconnect();
}
