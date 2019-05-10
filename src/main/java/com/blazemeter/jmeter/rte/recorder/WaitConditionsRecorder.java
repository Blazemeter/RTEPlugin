package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WaitConditionsRecorder {

  private SilentWaitRecorder silentWaitRecorder;
  private SyncWaitRecorder syncWaitRecorder;
  private long stablePeriodMillis;

  WaitConditionsRecorder(RteProtocolClient rteProtocolClient,
                         long timeoutThresholdMillis, long stablePeriodMillis) {
    syncWaitRecorder = new SyncWaitRecorder(rteProtocolClient,
        timeoutThresholdMillis, stablePeriodMillis, stablePeriodMillis);
    silentWaitRecorder = new SilentWaitRecorder(rteProtocolClient, timeoutThresholdMillis,
        stablePeriodMillis);
    this.stablePeriodMillis = stablePeriodMillis;
  }

  public void start() {
    syncWaitRecorder.start();
    silentWaitRecorder.start();
  }

  public List<WaitCondition> stop() {
    List<WaitCondition> waitConditions = new ArrayList<>();
    Date lastSyncInputInhibitedTime = syncWaitRecorder.getLastStatusChangeTime();
    Date lastSilentTime = silentWaitRecorder.getLastStatusChangeTime();

    if (syncWaitRecorder.buildWaitCondition().isPresent()) {
      waitConditions.add(syncWaitRecorder.buildWaitCondition().get());
      if (lastSilentTime.getTime() - lastSyncInputInhibitedTime.getTime() > stablePeriodMillis) {
        if (silentWaitRecorder.buildWaitCondition().isPresent()) {
          waitConditions.add(silentWaitRecorder.buildWaitCondition().get());
        }
      }
      return waitConditions;
    } else {
      waitConditions.add(silentWaitRecorder.buildWaitCondition().get());
      
      return waitConditions;

    }
  }

}
