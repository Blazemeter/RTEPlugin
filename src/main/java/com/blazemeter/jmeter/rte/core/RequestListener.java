package com.blazemeter.jmeter.rte.core;

public interface RequestListener extends RTEListener {

  long getLatency();

  long getEndTime();

}
