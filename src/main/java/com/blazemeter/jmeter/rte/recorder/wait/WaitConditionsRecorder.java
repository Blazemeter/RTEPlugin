package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.helger.commons.annotation.VisibleForTesting;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WaitConditionsRecorder {

  private final SilentWaitRecorder silentWaitRecorder;
  private final SyncWaitRecorder syncWaitRecorder;
  private final TextWaitRecorder textWaitRecorder;
  private final DisconnectWaitRecorder disconnectWaitRecorder;
  private final long stablePeriodMillis;

  public WaitConditionsRecorder(RteProtocolClient rteProtocolClient,
      long timeoutThresholdMillis, long stablePeriodMillis) {
    syncWaitRecorder = new SyncWaitRecorder(rteProtocolClient,
        timeoutThresholdMillis, stablePeriodMillis, stablePeriodMillis);
    silentWaitRecorder = new SilentWaitRecorder(rteProtocolClient, timeoutThresholdMillis,
        stablePeriodMillis);
    textWaitRecorder = new TextWaitRecorder(rteProtocolClient, timeoutThresholdMillis,
        stablePeriodMillis, stablePeriodMillis);
    disconnectWaitRecorder = new DisconnectWaitRecorder(rteProtocolClient,
        timeoutThresholdMillis, stablePeriodMillis);
    this.stablePeriodMillis = stablePeriodMillis;
  }

  @VisibleForTesting
  public WaitConditionsRecorder(SilentWaitRecorder silentWaitRecorder,
      SyncWaitRecorder syncWaitRecorder, TextWaitRecorder textWaitRecorder,
      DisconnectWaitRecorder disconnectWaitRecorder, long stablePeriodMillis) {
    this.silentWaitRecorder = silentWaitRecorder;
    this.syncWaitRecorder = syncWaitRecorder;
    this.textWaitRecorder = textWaitRecorder;
    this.stablePeriodMillis = stablePeriodMillis;
    this.disconnectWaitRecorder = disconnectWaitRecorder; 
  }

  public void start() {
    syncWaitRecorder.start();
    silentWaitRecorder.start();
    textWaitRecorder.start();
    disconnectWaitRecorder.start();
  }

  public List<WaitCondition> stop() {
    List<WaitCondition> waitConditions = new ArrayList<>();
    Optional<WaitCondition> disconnectWaitCondition = disconnectWaitRecorder.buildWaitCondition();
    if (disconnectWaitCondition.isPresent()) {
      waitConditions.add(disconnectWaitCondition.get());
      return waitConditions;
    }

    Optional<WaitCondition> textWaitCondition = textWaitRecorder.buildWaitCondition();
    Optional<WaitCondition> syncWaitCondition = syncWaitRecorder.buildWaitCondition();
    
    if (syncWaitCondition.isPresent()) {
      waitConditions.add(syncWaitCondition.get());
      Instant lastSyncInputInhibitedTime = syncWaitRecorder.getLastStatusChangeTime().orElse(null);
      Instant lastSilentTime = silentWaitRecorder.getLastStatusChangeTime().orElse(null);
      if ((lastSyncInputInhibitedTime != null) &&
          (ChronoUnit.MILLIS.between(lastSyncInputInhibitedTime,
              lastSilentTime) > stablePeriodMillis) && !textWaitCondition.isPresent()) {
        waitConditions.add(silentWaitRecorder.buildWaitCondition().orElse(null));
      }
    } else if (!textWaitCondition.isPresent()) {
      waitConditions.add(silentWaitRecorder.buildWaitCondition().orElse(null));

    }
    textWaitCondition.ifPresent(waitConditions::add);
    return waitConditions;
  }

  public void setWaitForTextCondition(String text) {
    textWaitRecorder.setWaitForTextCondition(text);
  }

}
