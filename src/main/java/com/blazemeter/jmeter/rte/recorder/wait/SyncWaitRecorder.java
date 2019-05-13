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

    if (rteProtocolClient.isInputInhibited() || lastStatusChangeTime == null) {
      return Optional.empty();
    }
    if (maxStablePeriodMillis > stablePeriodMillis) {
      LOG.warn("The period of time between the keyboard was locked and unlocked (" + 
          maxStablePeriodMillis + ") has exceed Stable Period(" + stablePeriodMillis +
          "), therefore a" +
              " Silent Wait condition will be added to your sampler." +
              " If you like to extend this period, just go throw settings");
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
