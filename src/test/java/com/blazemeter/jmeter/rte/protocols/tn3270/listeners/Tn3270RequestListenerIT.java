package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listeners.RequestListenerIT;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.TerminalClient;
import com.bytezone.dm3270.display.ScreenWatcher;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Tn3270RequestListenerIT extends
    RequestListenerIT<Tn3270RequestListener> {

  @Mock
  private Tn3270Client client;

  @Mock
  private TerminalClient terminalClient;

  @Mock
  private ScreenWatcher screenWatcher;

  @Override
  protected void generateScreenChangeEvent() {
    listener.screenChanged(screenWatcher);
  }

  @Override
  public Tn3270RequestListener buildRequestListener(SampleResult result) {
    return new Tn3270RequestListener(result, client, terminalClient);
  }

}
