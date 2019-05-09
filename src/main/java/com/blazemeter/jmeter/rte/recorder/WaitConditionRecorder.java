package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WaitConditionRecorder {

  protected static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);
  protected Date lastStatusChangeTime;
  protected long maxStablePeriod;
  protected RteProtocolClient rteProtocolClient;

  protected void onTerminalStatusChange() {
    Date currentTime = new Date();
    if (currentTime.getTime() - lastStatusChangeTime.getTime() > maxStablePeriod) {
      maxStablePeriod = currentTime.getTime() - lastStatusChangeTime.getTime();
    }
    lastStatusChangeTime = currentTime;

  }

  public abstract WaitCondition buildWaitCondition();

  public abstract void start();
}
