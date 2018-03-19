package com.blazemeter.jmeter.rte.core;

public interface RequestListener {

  long getLatency();

  long getEndTime();

  void stop();

}
