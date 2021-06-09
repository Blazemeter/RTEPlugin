package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.DisconnectWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import java.time.Clock;
import java.util.Optional;

public class DisconnectWaitRecorder extends WaitConditionRecorder {

  public DisconnectWaitRecorder(RteProtocolClient rteProtocolClient,
      long timeoutThresholdMillis, long stablePeriodThresholdMillis) {
    super(rteProtocolClient, timeoutThresholdMillis, stablePeriodThresholdMillis);
  }

  public DisconnectWaitRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
      long stablePeriodThresholdMillis, Clock clock) {
    super(rteProtocolClient, timeoutThresholdMillis, stablePeriodThresholdMillis, clock);
  }

  @Override
  public Optional<WaitCondition> buildWaitCondition() {
    if (rteProtocolClient.isServerDisconnected()) {
      return Optional.of(new DisconnectWaitCondition(buildTimeout()));
    }
    return Optional.empty();
  }
}
