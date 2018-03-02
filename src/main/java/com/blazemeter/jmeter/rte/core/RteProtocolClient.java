package com.blazemeter.jmeter.rte.core;

import java.util.List;
import java.util.concurrent.TimeoutException;

public interface RteProtocolClient {

  boolean isConnected();

  void connect(String server, int port, SSLType sslType, String password,
               String keyStorePath, TerminalType terminalType, long timeoutMillis,
               long stableTimeout) throws RteIOException, TimeoutException, InterruptedException;

  String send(List<CoordInput> input) throws InterruptedException;

  void disconnect();
}
