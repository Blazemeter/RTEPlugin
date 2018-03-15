package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.RequestListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.time.Duration;
import java.time.Instant;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tn5250RequestListener implements RequestListener, XI5250EmulatorListener {

  private static final Logger LOG = LoggerFactory.getLogger(RTESampler.class);

  protected final Tn5250Client client;
  private Instant startTime = Instant.now();
  private Instant firstResponseTime = Instant.now();
  private Instant lastResponseTime = Instant.now();
  private boolean receivedFirstResponse = false;

  public Tn5250RequestListener(Tn5250Client client) {
    this.client = client;
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
    if (!receivedFirstResponse) {
      receivedFirstResponse = true;
      firstResponseTime = Instant.now();
    }
    lastResponseTime = Instant.now();
    XI5250Emulator emulator = e.get5250Emulator();
    int height = emulator.getCrtSize().height;
    int width = emulator.getCrtSize().width;
    StringBuilder screen = new StringBuilder();
    for (int i = 0; i < height; i++) {
      screen.append(emulator.getString(0, i, width).replace("\u0000", " ").replace("\u0001", ""));
      screen.append("\n");
    }
    LOG.trace(screen.toString());
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent e) {
  }

  @Override
  public void dataSended(XI5250EmulatorEvent e) {

  }

  @Override
  public long getLatency() {
    return Duration.between(startTime, firstResponseTime).toMillis();
  }

  @Override
  public long getEndTime() {
    return lastResponseTime.getEpochSecond();
  }

  @Override
  public void stop() {
    client.removeListener(this);
  }
}
