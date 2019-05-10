package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.Optional;

public class SyncWaitRecorder extends WaitConditionRecorder {

  private final long stablePeriodMillis;
  private boolean lastInputInhibited = false;

  public SyncWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
                          long stablePeriodThresholdMillis, long stablePeriodMillis) {
    super(rteProtocolClient, timeoutThresholdMillis, stablePeriodThresholdMillis);
    this.stablePeriodMillis = stablePeriodMillis;
  }

  @Override
  public void onTerminalStateChange() {
    boolean inputInhibited = rteProtocolClient.isInputInhibited();
    if (lastInputInhibited != inputInhibited) {
      lastInputInhibited = inputInhibited;
      super.onTerminalStateChange();
    }

  }

  @Override
  public Optional<WaitCondition> buildWaitCondition() {

    if (rteProtocolClient.isInputInhibited() || lastStatusChangeTime ==null ) {
      return Optional.empty();
    }
    if (maxStablePeriodMillis > stablePeriodMillis) {
      LOG.warn("Wait Condition time out {}, your query has exceeded stable time period.",
          maxStablePeriodMillis);
      return Optional.empty();
    } else {
      return Optional.of(new SyncWaitCondition(buildTimeout(),
          stablePeriodMillis));
    }
  }

  @Override
  public void start() {
    super.start();
    lastInputInhibited = rteProtocolClient.isInputInhibited();
  }

}
