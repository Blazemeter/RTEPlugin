package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.SSLData;
import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface RteProtocolClient {

  boolean isConnected();

  void connect(String server, int port, SSLData sslData,
      TerminalType terminalType, long timeoutMillis, long stableTimeout)
      throws RteIOException, TimeoutException, InterruptedException;

  void send(List<CoordInput> input, Action action, List<WaitCondition> waitConditions)
      throws InterruptedException, TimeoutException, RteIOException;

  String getScreen();

  void disconnect() throws RteIOException;

  Dimension getScreenSize();

}
