package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import nl.lxtreme.jvt220.terminal.ScreenChangeListener;

public class Vt420TerminalStateListenerProxy implements ScreenChangeListener {

  private TerminalStateListener listener;

  public Vt420TerminalStateListenerProxy(TerminalStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void screenChanged(String s) {
    listener.onTerminalStateChange();
  }
}
