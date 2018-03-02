package com.blazemeter.jmeter.rte.virtualservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows to create a virtual service (a mock) a an actual service traffic dump.
 *
 * This is useful for testing clients and interactions which depend on a not always available
 * environment, either due to cost, resiliency, or other potential concerns.
 *
 * This class was required for tn5250 and tn3270 since <a href="http://www.mbtest.org/">mountebank</a>,
 * a potential open source alternative for implementing virtual tcp services, has no support for
 * sending initial packets from server (all interactions must be request based for mountebank). We
 * also discarded <a href="https://github.com/CloudRacer/MockTCPServer">MockTCPServer</a> which
 * required manual implementation of the protocol.
 *
 * TODO: move this to an independent project.
 */
public class VirtualTcpService implements Runnable {

  public static final int DEFAULT_READ_BUFFER_SIZE = 2048;

  private static final Logger LOG = LoggerFactory.getLogger(ClientPacket.class);

  private final int readBufferSize;
  private final ServerSocket server;
  private Flow flow;
  private ExecutorService serverExecutorService = Executors.newSingleThreadExecutor();
  private boolean stopped = false;
  private ClientConnection clientConnection;

  public VirtualTcpService(int port, int readBufferSize) throws IOException {
    server = new ServerSocket(port);
    this.readBufferSize = readBufferSize;
  }

  public VirtualTcpService(int port) throws IOException {
    this(port, DEFAULT_READ_BUFFER_SIZE);
  }

  public void setFlow(Flow flow) {
    this.flow = flow;
    Optional<PacketStep> bigPacketStep = flow.getSteps().stream()
        .filter(s -> s instanceof ClientPacket && s.data.getBytes().length > readBufferSize)
        .findAny();
    if (bigPacketStep.isPresent()) {
      throw new IllegalArgumentException(String.format(
          "Read buffer size of %d bytes is not enough for receiving expected packet from client "
              + "with %s", readBufferSize, bigPacketStep.get().data));
    }
  }

  public void start() {
    serverExecutorService.submit(this);
  }

  @Override
  public void run() {
    LOG.debug("Starting server on {} with flow: {}", server.getLocalPort(), flow);
    LOG.info("Waiting for connections on {}", server.getLocalPort());
    while (!stopped) {
      try {
        addClient(new ClientConnection(server.accept(), readBufferSize, flow));
        clientConnection.run();
      } catch (IOException e) {
        if (stopped) {
          LOG.trace("Received expected exception when server socket has been closed", e);
        } else {
          LOG.error("Problem waiting for client connection. Keep waiting.", e);
        }
      } finally {
        removeClient();
      }
    }
  }

  private synchronized void addClient(ClientConnection clientConnection) throws IOException {
    if (stopped) {
      clientConnection.close();
      return;
    }
    this.clientConnection = clientConnection;
  }

  private synchronized void removeClient() {
    clientConnection = null;
  }

  public void stop(long timeoutMillis) throws IOException, InterruptedException {
    synchronized (this) {
      stopped = true;
      server.close();
      if (clientConnection != null) {
        clientConnection.close();
      }
    }
    serverExecutorService.shutdown();
    if (!serverExecutorService.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
      LOG.warn("Server thread didn't stop after {} millis, interrupting it", timeoutMillis);
      serverExecutorService.shutdownNow();
    }
  }

}
