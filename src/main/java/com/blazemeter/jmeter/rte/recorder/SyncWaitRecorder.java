package com.blazemeter.jmeter.rte.recorder;

import java.util.Date;

public class SyncWaitRecorder extends WaitConditionRecorder {

  private Date lastStatusChangeTime = super.lastStatusChangeTime;
  private long maxStablePeriod = super.getMaxStablePeriod();
  private Date lastSyncInhibited;
  
  @Override
  protected void onTerminalStatusChange() {
    RTERecorder r = new RTERecorder();
    
    super.onTerminalStatusChange();
}

}