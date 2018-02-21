package com.blazemeter.jmeter.rte.core;

import java.util.List;

public interface RteProtocolClientListener {
    void connect(String server, int port);

    String send(List<CoordInput> input) throws InterruptedException;

    void disconnect();
}
