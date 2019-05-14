package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import java.awt.Dimension;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public interface RteProtocolClient {

  /**
   * Get the list of supported terminal types.
   *
   * @return The list of supported terminal types. First element in the list is used as default
   * value.
   */
  List<TerminalType> getSupportedTerminalTypes();

  default TerminalType getTerminalTypeById(String id) {
    return getSupportedTerminalTypes().stream()
        .filter(t -> id.equals(t.getId()))
        .findAny()
        .orElse(null);
  }

  default TerminalType getDefaultTerminalType() {
    return getSupportedTerminalTypes().get(0);
  }

  void connect(String server, int port, SSLType sslType, TerminalType terminalType,
      long timeoutMillis) throws RteIOException, InterruptedException, TimeoutException;

  void await(List<WaitCondition> waitConditions)
      throws InterruptedException, TimeoutException, RteIOException;

  void addTerminalStateListener(TerminalStateListener terminalStateListener);

  void removeTerminalStateListener(TerminalStateListener terminalStateListener);

  void send(List<Input> input, AttentionKey attentionKey) throws RteIOException;

  Screen getScreen();

  Dimension getScreenSize();

  boolean isInputInhibited();

  Optional<Position> getCursorPosition();

  boolean isAlarmOn();

  boolean resetAlarm();

  void disconnect() throws RteIOException;

}
