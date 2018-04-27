package com.blazemeter.jmeter.rte.core.listener;

public interface RequestListener {

  long getLatency();

  long getEndTime();

  void stop();

}
