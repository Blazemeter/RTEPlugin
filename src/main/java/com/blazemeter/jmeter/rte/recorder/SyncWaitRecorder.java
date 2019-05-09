package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.Date;

public class SyncWaitRecorder extends WaitConditionRecorder {

  private boolean lastInputInhibited = false;
  private long timeOutMillis = buildTimeOut();
  private Date startTime;
  private long stablePeriod;
  private RteProtocolClient rteProtocolClient;
  private long timeoutThresholdMillis;

  SyncWaitRecorder(RteProtocolClient rteProtocolClient,
                   long timeoutThresholdMillis, long stablePeriod) {
    super.rteProtocolClient = rteProtocolClient;
    this.rteProtocolClient = rteProtocolClient;
    this.timeoutThresholdMillis = timeoutThresholdMillis;
    this.stablePeriod = stablePeriod;
  }

  @Override
  protected void onTerminalStatusChange() {
    if (lastInputInhibited != rteProtocolClient.isInputInhibited()) {
      lastInputInhibited = rteProtocolClient.isInputInhibited();
      super.onTerminalStatusChange();
    }

  }

  @Override
  public WaitCondition buildWaitCondition() {
    if (maxStablePeriod > stablePeriod) {
      LOG.warn("Wait Condition time out, your query has exceeded stable time period.",
              maxStablePeriod);
      return null;
    } else {
      return new SyncWaitCondition(timeOutMillis, maxStablePeriod + timeoutThresholdMillis);
    }
  }

  @Override
  public void start() {
    startTime = new Date();

  }

  private long buildTimeOut() {
    return lastStatusChangeTime.getTime() - startTime.getTime();
  }
}
