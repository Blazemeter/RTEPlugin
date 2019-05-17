package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.helger.commons.annotation.VisibleForTesting;

import java.time.Clock;
import java.util.Optional;

public class SilentWaitRecorder extends WaitConditionRecorder {

  public SilentWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
                            long stablePeriodThresholdMillis) {
    super(rteProtocolClient, timeoutThresholdMillis, stablePeriodThresholdMillis);
  }

  @VisibleForTesting
  public SilentWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
                            long stablePeriodThresholdMillis, Clock clock) {
    super(rteProtocolClient, timeoutThresholdMillis, stablePeriodThresholdMillis, clock);

  }

  @Override
  public Optional<WaitCondition> buildWaitCondition() {
    return Optional.of(new SilentWaitCondition(buildTimeout(),
        maxStablePeriodMillis + stablePeriodThresholdMillis));
  }

  @Override
  public void onException(Throwable e) {
    
  }
}
