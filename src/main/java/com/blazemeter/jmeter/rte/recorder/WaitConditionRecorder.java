package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.Date;

public abstract class WaitConditionRecorder extends WaitConditionsRecorder {

  public long timeOutThreshold;
  private Date startTime = getStartTime();
  private Date lastStatusChangeTime;
  private long maxStablePeriod;

  protected void onTerminalStatusChange() {
    if (startTime.getTime() - lastStatusChangeTime.getTime() > maxStablePeriod) {
      maxStablePeriod = startTime.getTime() - lastStatusChangeTime.getTime();
    } else {
      lastStatusChangeTime = startTime;
    }
  }

  public abstract WaitCondition buildWaitCondition();

  public long getMaxStablePeriod() {
    return maxStablePeriod;
  }
}
