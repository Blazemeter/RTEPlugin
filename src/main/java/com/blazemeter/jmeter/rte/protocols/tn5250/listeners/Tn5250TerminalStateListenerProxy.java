package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;

public class Tn5250TerminalStateListenerProxy implements XI5250EmulatorListener {

  private final TerminalStateListener listener;

  public Tn5250TerminalStateListenerProxy(TerminalStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void connecting(XI5250EmulatorEvent e) {
  }

  @Override
  public void connected(XI5250EmulatorEvent e) {
  }

  @Override
  public void disconnected(XI5250EmulatorEvent e) {
  }

  @Override
  public void stateChanged(XI5250EmulatorEvent e) {
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent e) {
    listener.onTerminalStateChange();
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent e) {
  }

  @Override
  public void dataSended(XI5250EmulatorEvent e) {
  }

}
