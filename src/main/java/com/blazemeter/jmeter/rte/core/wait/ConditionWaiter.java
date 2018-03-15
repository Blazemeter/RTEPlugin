package com.blazemeter.jmeter.rte.core.wait;

import com.blazemeter.jmeter.rte.core.RTEListener;
import com.blazemeter.jmeter.rte.core.RteIOException;
import java.util.concurrent.TimeoutException;

public interface ConditionWaiter extends RTEListener{

  void await() throws InterruptedException, TimeoutException, RteIOException;

}
