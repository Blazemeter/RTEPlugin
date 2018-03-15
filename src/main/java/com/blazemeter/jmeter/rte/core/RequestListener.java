package com.blazemeter.jmeter.rte.core;

import java.util.Queue;

public interface RequestListener extends RTEListener{

  long getLatency();

  long getEndTime();

}
