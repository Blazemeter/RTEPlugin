package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.wait.ConnectionEndWaiter;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;

public class ConnectionEndTerminalListener implements XI5250EmulatorListener {

  private final ConnectionEndWaiter connectionEndWaiter;

  public ConnectionEndTerminalListener(ConnectionEndWaiter connectionEndWaiter) {
    this.connectionEndWaiter = connectionEndWaiter;
  }

  @Override
  public void connecting(XI5250EmulatorEvent e) {
  }

  @Override
  public void connected(XI5250EmulatorEvent e) {
    connectionEndWaiter.stop();
  }

  @Override
  public void disconnected(XI5250EmulatorEvent e) {
  }

  @Override
  public void stateChanged(XI5250EmulatorEvent e) {
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent e) {
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent e) {
  }

  @Override
  public void dataSended(XI5250EmulatorEvent e) {
  }

}
