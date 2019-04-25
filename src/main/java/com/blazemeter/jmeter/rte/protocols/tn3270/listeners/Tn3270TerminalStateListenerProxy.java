package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;

public class Tn3270TerminalStateListenerProxy implements ScreenChangeListener, CursorMoveListener,
    KeyboardStatusListener {

  private final TerminalStateListener listener;

  public Tn3270TerminalStateListenerProxy(TerminalStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent evt) {
    listener.onTerminalStateChange();
  }

  @Override
  public void cursorMoved(int oldLocation, int newLocation, Field field) {
    listener.onTerminalStateChange();
  }

  @Override
  public void screenChanged(ScreenWatcher screenWatcher) {
    listener.onTerminalStateChange();
  }
}
