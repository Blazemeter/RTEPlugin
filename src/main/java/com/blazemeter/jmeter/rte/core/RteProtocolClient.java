package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.ssl.SSLData;
import com.blazemeter.jmeter.rte.core.wait.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface RteProtocolClient {

  void connect(String server, int port, SSLData sslData,
      TerminalType terminalType, long timeoutMillis, long stableTimeout)
      throws RteIOException, TimeoutException, InterruptedException;

  List<? extends ConditionWaiter> buildConditionWaiters(List<WaitCondition> waitConditions);

  void send(List<CoordInput> input, Action action) throws RteIOException;

  String getScreen();

  boolean isInputInhibited();

  Position getCursorPosition();

  void disconnect() throws RteIOException;

}
