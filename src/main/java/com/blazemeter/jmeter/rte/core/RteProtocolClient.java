package com.blazemeter.jmeter.rte.core;

import java.util.List;

public interface RteProtocolClient {
    void connect(String server, int port);

    String send(List<CoordInput> input) throws InterruptedException;

    void disconnect();

    boolean isConnected();
}
