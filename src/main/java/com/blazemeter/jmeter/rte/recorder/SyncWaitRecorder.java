package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

public class SyncWaitRecorder extends WaitConditionRecorder {

  private boolean prevSyncStatusChange;
  private boolean syncStatus = getInputInhibitedStatus();

  @Override
  protected void onTerminalStatusChange() {
    if (syncStatus != prevSyncStatusChange) {
      prevSyncStatusChange = syncStatus;
      super.onTerminalStatusChange();
    }

  }

  @Override
  public WaitCondition buildWaitCondition() {
    long maxStablePeriod = getMaxStablePeriod();
    if (maxStablePeriod > stablePeriod) {
      return null;
    } else {
      return new SyncWaitCondition(timeOutThreshold, maxStablePeriod);
    }
  }
  
}
