package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.protocols.tn5250.ExtendedEmulator;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Tn5250RequestListenerIT {

  private Tn5250RequestListener listener;

  @Mock
  private Tn5250Client client;

  @Mock
  private ExtendedEmulator emulator;

  @Before
  public void setup() throws Exception {
    listener = new Tn5250RequestListener(client);
  }

  @Test
  public void shouldReturnGreaterLatencyThanTheElapsedTime() throws Exception {
    Thread.sleep(500);
    listener.newPanelReceived(
        new XI5250EmulatorEvent(XI5250EmulatorEvent.NEW_PANEL_RECEIVED, emulator));
    assertThat(listener.getLatency()).isGreaterThanOrEqualTo(500);
  }

  @Test
  public void shouldReturnGreaterEndTimeThanTheStartTime() throws Exception {
    long startTime = System.currentTimeMillis();
    /*This loop was included to simulate multiple screens sent by the server.
    The end time must be the time where the last screen came.*/
    for (int i = 0; i < 3; i++) {
      Thread.sleep(100);
      listener.newPanelReceived(
          new XI5250EmulatorEvent(XI5250EmulatorEvent.NEW_PANEL_RECEIVED, emulator));
    }
    assertThat(listener.getEndTime()).isGreaterThanOrEqualTo(startTime + 300);
  }
}
