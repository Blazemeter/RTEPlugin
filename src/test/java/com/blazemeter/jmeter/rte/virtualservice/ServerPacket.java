package com.blazemeter.jmeter.rte.virtualservice;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A step in a flow which sends a packet to the client.
 */
public class ServerPacket extends PacketStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientPacket.class);

  public ServerPacket() {}

  public ServerPacket(String hexDump) {
    super(hexDump);
  }

  @Override
  public void process(ClientConnection clientConnection) {
    LOGGER.debug("sending {}", data);
    try {
      clientConnection.write(data.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    return "server: " + data;
  }

}
