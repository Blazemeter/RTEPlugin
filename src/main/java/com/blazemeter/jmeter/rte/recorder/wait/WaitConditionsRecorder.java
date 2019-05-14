package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WaitConditionsRecorder {

  private SilentWaitRecorder silentWaitRecorder;
  private SyncWaitRecorder syncWaitRecorder;
  private long stablePeriodMillis;

  public WaitConditionsRecorder(RteProtocolClient rteProtocolClient,
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
    
    Optional<WaitCondition> syncWaitCondition = syncWaitRecorder.buildWaitCondition();
    if (syncWaitCondition.isPresent()) {
      waitConditions.add(syncWaitCondition.get());
      Instant lastSyncInputInhibitedTime = syncWaitRecorder.getLastStatusChangeTime().orElse(null);
      Instant lastSilentTime = silentWaitRecorder.getLastStatusChangeTime().orElse(null);
      if (lastSilentTime.getEpochSecond() - lastSyncInputInhibitedTime.getEpochSecond() > stablePeriodMillis) {

        waitConditions.add(silentWaitRecorder.buildWaitCondition().orElse(null));

      }
      return waitConditions;
    } else {
      waitConditions.add(silentWaitRecorder.buildWaitCondition().orElse(null));

      return waitConditions;

    }
  }

}
