package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.function.Supplier;

public enum Protocol {
  TN5250(Tn5250Client::new),
  TN3270(null);

  private final Supplier<RteProtocolClient> factory;

  Protocol(Supplier<RteProtocolClient> s) {
    this.factory = s;
  }

  public RteProtocolClient createProtocolClient() {
    return factory.get();
  }
}

