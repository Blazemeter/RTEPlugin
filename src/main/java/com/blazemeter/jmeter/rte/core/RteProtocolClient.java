package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.protocols.tn5250.RteIOException;
import java.util.List;

public interface RteProtocolClient {

  void connect(String server, int port, TerminalType terminalType) throws RteIOException;

  String send(List<CoordInput> input) throws InterruptedException;

  void disconnect();

  boolean isConnected();
}
