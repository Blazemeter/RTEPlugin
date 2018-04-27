package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.time.Duration;
import java.time.Instant;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tn5250RequestListener implements RequestListener, XI5250EmulatorListener {

  private static final Logger LOG = LoggerFactory.getLogger(Tn5250RequestListener.class);

  private final Tn5250Client client;
  private Instant startTime = Instant.now();
  private Instant firstResponseTime = Instant.now();
  private long lastResponseTime = System.currentTimeMillis();
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
    lastResponseTime = System.currentTimeMillis();
    if (LOG.isTraceEnabled()) {
      LOG.trace(client.getScreen());
    }
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
    return lastResponseTime;
  }

  @Override
  public void stop() {
    client.removeListener(this);
  }
}
