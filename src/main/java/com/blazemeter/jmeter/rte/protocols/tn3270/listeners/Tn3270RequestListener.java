package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.TerminalClient;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;
import org.apache.jmeter.samplers.SampleResult;

public class Tn3270RequestListener extends RequestListener<Tn3270Client> implements
    ScreenChangeListener {

  private final TerminalClient terminalClient;

  public Tn3270RequestListener(SampleResult result, Tn3270Client client,
      TerminalClient terminalClient) {
    super(result, client);
    this.terminalClient = terminalClient;
    terminalClient.addScreenChangeListener(this);
  }

  @Override
  public void stop() {
    super.stop();
    terminalClient.removeScreenChangeListener(this);
  }

  @Override
  public void screenChanged(ScreenWatcher screenWatcher) {
    newScreenReceived();
  }

}
