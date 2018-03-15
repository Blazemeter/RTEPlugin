package com.blazemeter.jmeter.rte.core.wait;

import com.blazemeter.jmeter.rte.core.RteIOException;
import java.util.concurrent.TimeoutException;

public interface ConditionWaiter {

  void await() throws InterruptedException, TimeoutException, RteIOException;

  void stop();

}
