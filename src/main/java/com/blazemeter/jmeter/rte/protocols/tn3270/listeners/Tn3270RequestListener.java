package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;

public class Tn3270RequestListener extends RequestListener<Tn3270Client> implements
    ScreenChangeListener {

  private final Screen screen;

  public Tn3270RequestListener(Tn3270Client client, Screen screen) {
    super(client);
    this.screen = screen;
  }

  @Override
  public void stop() {
    screen.getFieldManager().removeScreenChangeListener(this);
  }

  @Override
  public void screenChanged(ScreenWatcher screenWatcher) {
    newScreenReceived();
  }
}
