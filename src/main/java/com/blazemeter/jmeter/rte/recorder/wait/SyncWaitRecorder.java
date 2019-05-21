package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.helger.commons.annotation.VisibleForTesting;

import java.time.Clock;
import java.util.Optional;

public class SyncWaitRecorder extends WaitConditionRecorder {

  private long stablePeriodMillis;
  private boolean lastInputInhibited = false;

  public SyncWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
                          long stablePeriodThresholdMillis, long stablePeriodMillis) {
    super(rteProtocolClient, timeoutThresholdMillis, stablePeriodThresholdMillis);
    this.stablePeriodMillis = stablePeriodMillis;
  }

  @VisibleForTesting
  public SyncWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
                          long stablePeriodMillis, Clock clock) {
    super(rteProtocolClient, timeoutThresholdMillis, stablePeriodMillis, clock);
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
    if (rteProtocolClient.isInputInhibited()) {
      return Optional.empty();
    }
    if (maxStablePeriodMillis > stablePeriodMillis) {
      LOG.warn("Period between keyboard status changes ({}) has been greater than" +
          " stable period ({}), so, a wait for sync is not appropriate for this interaction! " +
          "You might increase stable period (as described in readme) and do a new recording if" +
          " you think current value is too small.", maxStablePeriodMillis, stablePeriodMillis);
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
